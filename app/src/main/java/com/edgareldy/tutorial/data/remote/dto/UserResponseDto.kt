package com.edgareldy.tutorial.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for the backend UserResponse schema.
 * Maps the snake_case JSON fields from /auth/register, /auth/me and /auth/activate-account.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@Serializable
data class UserResponseDto(
    @SerialName("id") val id: Long,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("enabled") val enabled: Boolean = false,
    @SerialName("account_locked") val accountLocked: Boolean = false
)
