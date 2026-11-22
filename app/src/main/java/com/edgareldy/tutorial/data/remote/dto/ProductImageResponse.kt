package com.edgareldy.tutorial.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for a product image attachment as returned by the API.
 * @SerialName maps each snake_case JSON field to its camelCase Kotlin property.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Serializable
data class ProductImageResponse(
    @SerialName("id") val id: Long,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("alt_text") val altText: String? = null,
    @SerialName("is_primary") val isPrimary: Boolean = false,
    @SerialName("display_order") val displayOrder: Int = 0
)
