package com.edgareldy.tutorial.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for the order header as returned by GET /api/v1/orders and POST /api/v1/orders.
 * The items field is optional in the list response (backend may omit it
 * for performance) but always present in the detail response.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Serializable
data class OrderResponse(
    @SerialName("id") val id: Long = 0L,
    @SerialName("order_number") val orderNumber: String = "",
    @SerialName("status") val status: String = "",
    @SerialName("total_amount") val totalAmount: Double = 0.0,
    @SerialName("items") val items: List<OrderItemResponse> = emptyList()
)
