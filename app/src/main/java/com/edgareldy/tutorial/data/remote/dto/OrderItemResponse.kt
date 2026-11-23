package com.edgareldy.tutorial.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for a single line item within an order response from the backend.
 * All snake_case field names are mapped via @SerialName to follow the
 * project convention: JSON keys come from the API spec, Kotlin properties
 * use camelCase.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Serializable
data class OrderItemResponse(
    @SerialName("id") val id: Long = 0L,
    @SerialName("product_id") val productId: Long = 0L,
    @SerialName("product_name") val productName: String = "",
    @SerialName("unit_price") val unitPrice: Double = 0.0,
    @SerialName("quantity") val quantity: Int = 0,
    @SerialName("subtotal") val subtotal: Double = 0.0
)
