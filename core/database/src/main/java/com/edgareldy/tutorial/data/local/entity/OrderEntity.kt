package com.edgareldy.tutorial.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a cached order header.
 * Stores only the scalar fields of an order. The related items are stored
 * separately in OrderItemEntity and joined by orderId.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: Long,
    val orderNumber: String,
    val status: String,
    val totalAmount: Double,
    val cachedAt: Long = System.currentTimeMillis()
)
