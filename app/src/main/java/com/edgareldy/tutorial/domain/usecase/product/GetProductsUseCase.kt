package com.edgareldy.tutorial.domain.usecase.product

import com.edgareldy.tutorial.domain.model.Product
import com.edgareldy.tutorial.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Returns a Flow of products, optionally filtered by category, search query
 * and price range. Delegates all business logic to [ProductRepository] which
 * applies the cache-first strategy transparently.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class GetProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(
        categoryId: Long? = null,
        search: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        page: Int = 0,
        size: Int = 20
    ): Flow<Result<List<Product>>> =
        repository.getProducts(categoryId, search, minPrice, maxPrice, page, size)
}
