package com.edgareldy.tutorial.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a single line item within a cached order.
 * Declares a ForeignKey to OrderEntity so that deleting an order
 * cascades and removes all its orphaned items automatically.
 * An index on orderId speeds up the per-order item queries.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("orderId")]
)
data class OrderItemEntity(
    @PrimaryKey val id: Long,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val unitPrice: Double,
    val quantity: Int,
    val subtotal: Double
)
