package com.edgareldy.tutorial.orders.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for a single line item within an order, as returned by the backend.
 * Field names follow the OpenAPI schema: snake_case for business fields
 * (product_id, product_name, unit_price) and plain names for numeric scalars.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
@Serializable
data class OrderItemResponse(
    @SerialName("id") val id: Long,
    @SerialName("product_id") val productId: Long,
    @SerialName("product_name") val productName: String,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("quantity") val quantity: Int,
    @SerialName("subtotal") val subtotal: Double
)
