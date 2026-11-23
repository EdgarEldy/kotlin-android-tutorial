package com.edgareldy.tutorial.domain.model

/**
 * Domain model representing a shopping cart with its line items and server-calculated total.
 * No Android or persistence dependencies - pure Kotlin data classes used across all layers.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
data class Cart(
    val id: Long,
    val items: List<CartItem>,
    val total: Double
)

/**
 * A single line item inside a [Cart], holding product info and quantity.
 * subtotal is computed server-side (unitPrice * quantity) and must not be
 * recalculated on the client.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
data class CartItem(
    val id: Long,
    val productId: Long,
    val productName: String,
    val unitPrice: Double,
    val quantity: Int,
    val subtotal: Double
)
