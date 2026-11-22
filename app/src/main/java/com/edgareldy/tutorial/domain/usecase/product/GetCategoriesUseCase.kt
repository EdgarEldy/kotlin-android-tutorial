package com.edgareldy.tutorial.domain.usecase.product

import com.edgareldy.tutorial.domain.model.Category
import com.edgareldy.tutorial.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Returns a Flow of all cached categories, refreshing from the network when
 * the local cache is empty. Used by ProductListViewModel to populate the
 * category filter chip row.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class GetCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    operator fun invoke(): Flow<Result<List<Category>>> = repository.getCategories()
}
