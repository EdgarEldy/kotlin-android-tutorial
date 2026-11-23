package com.edgareldy.tutorial.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.edgareldy.tutorial.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for the categories table.
 * Uses @Upsert (INSERT OR REPLACE) for per-row updates and @Query DELETE for
 * full cache invalidation before a network refresh, so categories removed on
 * the backend do not persist as phantom entries in the local cache.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Dao
interface CategoryDao {

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Query("SELECT * FROM categories ORDER BY categoryName ASC")
    fun getCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun getCategoryById(id: Long): Flow<CategoryEntity?>

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
