package com.edgareldy.tutorial.domain.model

/**
 * Domain model representing an order placed by the current user.
 * Holds only the data needed by the UI: header fields plus the list of
 * line items. No Room or Retrofit annotations - this model is framework-free.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
data class Order(
    val id: Long,
    val orderNumber: String,
    val status: OrderStatus,
    val totalAmount: Double,
    val items: List<OrderItem>
)

/**
 * Enum representing all possible lifecycle states of an order as returned
 * by the backend. The UI uses this to render color-coded status badges.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
enum class OrderStatus {
    PENDING,
    CONFIRMED,
    CANCELLED
}

/**
 * Domain model for a single line item within an order.
 * The subtotal is provided by the backend and must not be recomputed
 * on the client to avoid price discrepancies.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
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
