package com.edgareldy.tutorial.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for the cart resource as returned by all /api/v1/cart/ endpoints.
 * items defaults to an empty list so an empty cart deserializes without errors.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Serializable
data class CartResponse(
    @SerialName("id") val id: Long,
    @SerialName("items") val items: List<CartItemResponse> = emptyList(),
    @SerialName("total") val total: Double = 0.0
)
