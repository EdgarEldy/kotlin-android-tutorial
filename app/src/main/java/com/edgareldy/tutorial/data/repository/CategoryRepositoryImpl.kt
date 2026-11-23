package com.edgareldy.tutorial.data.repository

import com.edgareldy.tutorial.core.di.ApplicationScope
import com.edgareldy.tutorial.data.local.dao.CategoryDao
import com.edgareldy.tutorial.data.local.entity.CategoryEntity
import com.edgareldy.tutorial.data.remote.api.CategoryApiService
import com.edgareldy.tutorial.domain.model.Category
import com.edgareldy.tutorial.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache-first implementation of [CategoryRepository].
 * Room emits the cached category list immediately; a background refresh
 * writes any new categories from the network, triggering a second emission.
 * This mirrors the pattern used in [ProductRepositoryImpl].
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryApiService: CategoryApiService,
    private val categoryDao: CategoryDao,
    @ApplicationScope private val scope: CoroutineScope
) : CategoryRepository {

    override fun getCategories(): Flow<Result<List<Category>>> =
        categoryDao.getCategories()
            .map { rows -> Result.success(rows.map { it.toDomain() }) }
            .onStart {
                scope.launch {
                    runCatching { refreshCategories() }
                }
            }

    override suspend fun refreshCategories() {
        val response = categoryApiService.getCategories(page = 0, size = 100)
        val page = response.data ?: return
        val entities = page.content.map { dto ->
            CategoryEntity(id = dto.id, categoryName = dto.categoryName)
        }
        categoryDao.deleteAll()
        categoryDao.upsertAll(entities)
    }

    // --- Mapper ---

    private fun CategoryEntity.toDomain() = Category(id = id, name = categoryName)
}
