package com.edgareldy.tutorial.domain.usecase.auth

import com.edgareldy.tutorial.domain.repository.PasswordResetRepository
import javax.inject.Inject

/**
 * Step 2 of the mobile password reset flow.
 * Verifies that the OTP is valid without consuming it, allowing the user
 * to proceed to the new-password screen with confidence.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
class VerifyPasswordResetOtpUseCase @Inject constructor(
    private val passwordResetRepository: PasswordResetRepository
) {
    suspend operator fun invoke(email: String, otp: String): Result<Unit> {
        if (email.isBlank() || otp.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and OTP must not be blank"))
        }
        return passwordResetRepository.verifyOtp(email.trim(), otp.trim())
    }
}
