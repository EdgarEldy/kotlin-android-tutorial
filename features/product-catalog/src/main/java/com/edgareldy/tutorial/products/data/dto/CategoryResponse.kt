package com.edgareldy.tutorial.products.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for a category as returned by GET /api/v1/categories/.
 * products is included for completeness but is not used in the app - the
 * products list is handled separately through ProductRepository.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Serializable
data class CategoryResponse(
    @SerialName("id") val id: Long,
    @SerialName("category_name") val categoryName: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("products") val products: List<ProductResponse> = emptyList()
)
