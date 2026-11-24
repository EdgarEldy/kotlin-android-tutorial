package com.edgareldy.tutorial.auth.data.api

import com.edgareldy.tutorial.auth.data.dto.ApiResponse
import com.edgareldy.tutorial.auth.data.dto.ConfirmResetRequest
import com.edgareldy.tutorial.auth.data.dto.SendOtpRequest
import com.edgareldy.tutorial.auth.data.dto.VerifyOtpRequest
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit service for the 3-step mobile password reset flow.
 * Never calls the web-only /auth/forgot-password or /auth/reset-password endpoints.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
interface PasswordResetApiService {

    /** Step 1: request an OTP sent to the user's email. */
    @POST("api/v1/password-reset/mobile")
    suspend fun sendOtp(@Body request: SendOtpRequest): ApiResponse<Unit>

    /** Step 2: validate the OTP without consuming it. */
    @POST("api/v1/password-reset/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): ApiResponse<Unit>

    /**
     * Step 3: reset the password.
     * The OTP is passed as the [token] query parameter per the OpenAPI spec.
     */
    @POST("api/v1/password-reset/confirm")
    suspend fun confirmReset(
        @Query("token") token: String,
        @Body request: ConfirmResetRequest
    ): ApiResponse<Unit>
}
