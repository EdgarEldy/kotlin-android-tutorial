package com.edgareldy.tutorial.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for POST /api/v1/cart/items.
 * product_id and quantity are the only fields the backend expects for adding an item.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Serializable
data class AddCartItemRequest(
    @SerialName("product_id") val productId: Long,
    @SerialName("quantity") val quantity: Int
)
