package com.edgareldy.tutorial.products.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for a product as returned by GET /api/v1/products/{id} or the page content.
 * images defaults to an empty list so older API responses without the field
 * deserialise without throwing a missing-field exception.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Serializable
data class ProductResponse(
    @SerialName("id") val id: Long,
    @SerialName("product_name") val productName: String,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("description") val description: String? = null,
    @SerialName("active") val active: Boolean = true,
    @SerialName("category_id") val categoryId: Long? = null,
    @SerialName("category_name") val categoryName: String? = null,
    @SerialName("images") val images: List<ProductImageResponse> = emptyList()
)
