package com.edgareldy.tutorial.orders.domain.model

/**
 * Enum representing the lifecycle states of an order as defined by the backend.
 * Values are matched case-insensitively against the raw string status from the API.
 * UNKNOWN absorbs any future status values the backend may introduce.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
enum class OrderStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    UNKNOWN
}

/**
 * Domain model for a single order line item. No Retrofit or Room dependency -
 * this class is mapped from OrderItemResponse by the repository layer.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
data class OrderItem(
    val id: Long,
    val productId: Long,
    val productName: String,
    val unitPrice: Double,
    val quantity: Int,
    val subtotal: Double
)

/**
 * Domain model for an order. Aggregates the header fields and line items.
 * This is the type exposed to ViewModels and Composables - it carries no
 * framework annotations (Retrofit, Room, Serialization).
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
data class Order(
    val id: Long,
    val orderNumber: String,
    val status: OrderStatus,
    val totalAmount: Double,
    val items: List<OrderItem>
)
