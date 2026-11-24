package com.edgareldy.tutorial.orders.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Generic API envelope returned by every Spring Boot endpoint.
 * Local copy in :feature:orders to keep the module self-contained.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
@Serializable
data class ApiResponse<T>(
    @SerialName("success") val success: Boolean = false,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: T? = null,
    @SerialName("timestamp") val timestamp: String? = null
)
