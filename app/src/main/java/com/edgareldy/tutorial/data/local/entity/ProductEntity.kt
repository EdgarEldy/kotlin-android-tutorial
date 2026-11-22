package com.edgareldy.tutorial.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a cached product row.
 * cachedAt records the epoch timestamp of the last network refresh so the
 * repository can decide whether a cache-first read is still fresh enough.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Long,
    val productName: String,
    val unitPrice: Double,
    val description: String?,
    val active: Boolean,
    val categoryId: Long?,
    val categoryName: String?,
    val cachedAt: Long = System.currentTimeMillis()
)
