package com.edgareldy.tutorial.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for PATCH /api/v1/cart/items/{itemId}.
 * Only the new quantity is sent; the backend infers the item from the path variable.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Serializable
data class UpdateCartItemRequest(
    @SerialName("quantity") val quantity: Int
)
