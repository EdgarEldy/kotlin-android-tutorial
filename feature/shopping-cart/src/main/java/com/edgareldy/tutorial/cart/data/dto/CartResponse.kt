package com.edgareldy.tutorial.cart.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for the cart resource as returned by all /api/v1/cart/ endpoints.
 * id is nullable because a new account with an empty cart may return null before
 * the first item is added. items defaults to an empty list so an empty cart
 * deserializes without errors.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
@Serializable
data class CartResponse(
    @SerialName("id") val id: Long? = null,
    @SerialName("items") val items: List<CartItemResponse> = emptyList(),
    @SerialName("total") val total: Double = 0.0
)
