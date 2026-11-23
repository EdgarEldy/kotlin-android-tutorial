package com.edgareldy.tutorial.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for a single line item inside a CartResponse.
 * All monetary fields come from the server and must not be recalculated on the client.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Serializable
data class CartItemResponse(
    @SerialName("id") val id: Long,
    @SerialName("product_id") val productId: Long,
    @SerialName("product_name") val productName: String,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("quantity") val quantity: Int,
    @SerialName("subtotal") val subtotal: Double
)
