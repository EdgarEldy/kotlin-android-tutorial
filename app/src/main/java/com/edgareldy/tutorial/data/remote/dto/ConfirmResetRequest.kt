package com.edgareldy.tutorial.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for POST /api/v1/password-reset/confirm.
 * Maps the ResetPasswordRequest backend schema.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@Serializable
data class ConfirmResetRequest(
    @SerialName("new_password") val newPassword: String,
    @SerialName("confirm_password") val confirmPassword: String
)
