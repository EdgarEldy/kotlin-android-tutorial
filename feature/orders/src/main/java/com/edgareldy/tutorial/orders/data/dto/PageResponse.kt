package com.edgareldy.tutorial.orders.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Pagination envelope returned by list endpoints.
 * The backend uses camelCase for pagination fields (totalElements, totalPages)
 * while business fields use snake_case - both are expressed via @SerialName.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
@Serializable
data class PageResponse<T>(
    @SerialName("content") val content: List<T> = emptyList(),
    @SerialName("page") val page: Int = 0,
    @SerialName("size") val size: Int = 0,
    @SerialName("totalElements") val totalElements: Long = 0L,
    @SerialName("totalPages") val totalPages: Int = 0
)
