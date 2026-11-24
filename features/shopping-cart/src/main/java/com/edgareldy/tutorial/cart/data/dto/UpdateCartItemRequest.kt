package com.edgareldy.tutorial.cart.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for PATCH /api/v1/cart/items/{itemId}.
 * Only the new quantity is sent; the backend infers the item from the path variable.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
@Serializable
data class UpdateCartItemRequest(
    @SerialName("quantity") val quantity: Int
)
