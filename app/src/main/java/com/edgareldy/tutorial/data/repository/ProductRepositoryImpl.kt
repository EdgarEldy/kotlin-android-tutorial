package com.edgareldy.tutorial.data.repository

import com.edgareldy.tutorial.data.local.dao.ProductDao
import com.edgareldy.tutorial.data.local.entity.ProductEntity
import com.edgareldy.tutorial.data.local.entity.ProductImageEntity
import com.edgareldy.tutorial.data.local.entity.ProductWithImages
import com.edgareldy.tutorial.data.remote.api.ProductApiService
import com.edgareldy.tutorial.data.remote.dto.ProductResponse
import com.edgareldy.tutorial.domain.model.Product
import com.edgareldy.tutorial.domain.model.ProductImage
import com.edgareldy.tutorial.core.di.ApplicationScope
import com.edgareldy.tutorial.domain.repository.ProductRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache-first implementation of [ProductRepository].
 * On each collection: Room emits the current cache immediately, then a
 * background refresh writes updated rows into Room which triggers a second
 * emission automatically via the DAO Flow. Network errors are swallowed
 * when the cache is non-empty so the UI stays functional offline.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productApiService: ProductApiService,
    private val productDao: ProductDao,
    @ApplicationScope private val scope: CoroutineScope
) : ProductRepository {

    override fun getProducts(
        categoryId: Long?,
        page: Int,
        size: Int
    ): Flow<Result<List<Product>>> {
        val daoFlow = if (categoryId != null) {
            productDao.getProductsByCategoryId(categoryId)
        } else {
            productDao.getProducts()
        }

        return daoFlow
            .map { rows -> Result.success(rows.map { pwi: ProductWithImages -> pwi.toDomain() }) }
            .onStart {
                scope.launch {
                    runCatching {
                        refreshProducts(categoryId)
                    }
                }
            }
    }

    override fun getProductById(id: Long): Flow<Result<Product>> =
        productDao.getProductById(id)
            .map { row ->
                if (row != null) {
                    Result.success(row.toDomain())
                } else {
                    Result.failure(NoSuchElementException("Product $id not found in cache"))
                }
            }
            .onStart {
                scope.launch {
                    runCatching {
                        val response = productApiService.getProductById(id)
                        val dto = response.data ?: return@runCatching
                        val entity = dto.toEntity()
                        productDao.upsertAll(listOf(entity))
                        val imageEntities = dto.images.map { img ->
                            ProductImageEntity(
                                id = img.id,
                                productId = dto.id,
                                imageUrl = img.imageUrl,
                                altText = img.altText,
                                isPrimary = img.isPrimary,
                                displayOrder = img.displayOrder
                            )
                        }
                        if (imageEntities.isNotEmpty()) {
                            productDao.upsertImages(imageEntities)
                        }
                    }
                }
            }

    override suspend fun refreshProducts(categoryId: Long?) {
        val response = productApiService.getProducts(
            categoryId = categoryId,
            page = 0,
            size = 100
        )
        val page = response.data ?: return
        val entities = page.content.map { it.toEntity() }
        productDao.upsertAll(entities)
        val imageEntities = page.content.flatMap { dto ->
            dto.images.map { img ->
                ProductImageEntity(
                    id = img.id,
                    productId = dto.id,
                    imageUrl = img.imageUrl,
                    altText = img.altText,
                    isPrimary = img.isPrimary,
                    displayOrder = img.displayOrder
                )
            }
        }
        if (imageEntities.isNotEmpty()) {
            productDao.upsertImages(imageEntities)
        }
    }

    // --- Mappers ---

    private fun ProductResponse.toEntity() = ProductEntity(
        id = id,
        productName = productName,
        unitPrice = unitPrice,
        description = description,
        active = active,
        categoryId = categoryId,
        categoryName = categoryName
    )

    private fun ProductWithImages.toDomain() = Product(
        id = product.id,
        name = product.productName,
        unitPrice = product.unitPrice,
        description = product.description,
        active = product.active,
        categoryId = product.categoryId,
        categoryName = product.categoryName,
        images = images.sortedBy { it.displayOrder }.map { img ->
            ProductImage(
                id = img.id,
                imageUrl = img.imageUrl,
                altText = img.altText,
                isPrimary = img.isPrimary,
                displayOrder = img.displayOrder
            )
        }
    )
}
