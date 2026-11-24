package com.edgareldy.tutorial.auth.data.repository

import com.edgareldy.tutorial.auth.data.api.PasswordResetApiService
import com.edgareldy.tutorial.auth.data.dto.ConfirmResetRequest
import com.edgareldy.tutorial.auth.data.dto.SendOtpRequest
import com.edgareldy.tutorial.auth.data.dto.VerifyOtpRequest
import com.edgareldy.tutorial.auth.domain.repository.PasswordResetRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [PasswordResetRepository] backed by [PasswordResetApiService].
 * Delegates each step of the 3-step mobile OTP flow to the corresponding endpoint.
 * The OTP value is never logged or persisted here.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@Singleton
class PasswordResetRepositoryImpl @Inject constructor(
    private val passwordResetApiService: PasswordResetApiService
) : PasswordResetRepository {

    override suspend fun sendOtp(email: String): Result<Unit> = runCatching {
        val response = passwordResetApiService.sendOtp(SendOtpRequest(email))
        if (!response.success) {
            error(response.message ?: "Failed to send OTP")
        }
    }

    override suspend fun verifyOtp(email: String, otp: String): Result<Unit> = runCatching {
        val response = passwordResetApiService.verifyOtp(VerifyOtpRequest(email, otp))
        if (!response.success) {
            error(response.message ?: "OTP verification failed")
        }
    }

    override suspend fun confirmReset(
        otp: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Unit> = runCatching {
        val response = passwordResetApiService.confirmReset(
            token = otp,
            request = ConfirmResetRequest(newPassword, confirmPassword)
        )
        if (!response.success) {
            error(response.message ?: "Password reset failed")
        }
    }
}
