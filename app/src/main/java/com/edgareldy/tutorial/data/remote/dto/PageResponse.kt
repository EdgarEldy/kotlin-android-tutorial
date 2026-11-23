package com.edgareldy.tutorial.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Generic pagination wrapper returned by all paginated endpoints.
 * content holds the typed items for the current page. totalElements and
 * totalPages allow the UI to show paging indicators or load-more triggers.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
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
