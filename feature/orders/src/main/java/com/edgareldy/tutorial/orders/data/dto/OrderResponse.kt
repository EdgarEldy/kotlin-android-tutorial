package com.edgareldy.tutorial.orders.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for an order resource as returned by all /api/v1/orders endpoints.
 * items defaults to an empty list so an order without items deserializes safely.
 * The status field is a plain String here; the domain layer maps it to OrderStatus.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
@Serializable
data class OrderResponse(
    @SerialName("id") val id: Long,
    @SerialName("order_number") val orderNumber: String,
    @SerialName("status") val status: String,
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("items") val items: List<OrderItemResponse> = emptyList()
)
