package com.edgareldy.tutorial.products.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Generic envelope wrapping every backend response.
 * The data field is nullable to handle Void responses where the backend
 * returns null in that field.
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
