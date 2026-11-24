package com.edgareldy.tutorial.auth.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for POST /api/v1/password-reset/mobile.
 * Uses the ForgotPasswordRequest backend schema (email only).
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@Serializable
data class SendOtpRequest(
    @SerialName("email") val email: String
)
