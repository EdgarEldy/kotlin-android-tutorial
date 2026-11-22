package com.edgareldy.tutorial.domain.repository

import com.edgareldy.tutorial.domain.model.Category
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for category data.
 * Categories are small and rarely change, so a single getCategories() flow
 * covers all UI needs. refreshCategories triggers an explicit network sync
 * on pull-to-refresh or first launch.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
interface CategoryRepository {

    fun getCategories(): Flow<Result<List<Category>>>

    suspend fun refreshCategories()
}
