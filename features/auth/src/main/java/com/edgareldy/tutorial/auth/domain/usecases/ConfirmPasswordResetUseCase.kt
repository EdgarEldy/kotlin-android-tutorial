package com.edgareldy.tutorial.auth.domain.usecase

import com.edgareldy.tutorial.auth.domain.repository.PasswordResetRepository
import javax.inject.Inject

/**
 * Step 3 of the mobile password reset flow.
 * Confirms the new password using the OTP that was verified in step 2.
 * Client-side rule: new password and confirmation must match.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
class ConfirmPasswordResetUseCase @Inject constructor(
    private val passwordResetRepository: PasswordResetRepository
) {
    suspend operator fun invoke(
        otp: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Unit> {
        if (newPassword.isBlank()) {
            return Result.failure(IllegalArgumentException("Password must not be blank"))
        }
        if (newPassword != confirmPassword) {
            return Result.failure(IllegalArgumentException("Passwords do not match"))
        }
        return passwordResetRepository.confirmReset(otp, newPassword, confirmPassword)
    }
}
