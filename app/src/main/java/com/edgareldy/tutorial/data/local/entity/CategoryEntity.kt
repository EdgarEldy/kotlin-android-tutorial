package com.edgareldy.tutorial.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a cached product category.
 * cachedAt mirrors the pattern used in ProductEntity to track refresh
 * freshness independently from the products table.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: Long,
    val categoryName: String,
    val cachedAt: Long = System.currentTimeMillis()
)
