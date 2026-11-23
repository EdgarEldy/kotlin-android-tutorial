package com.edgareldy.tutorial.domain.usecase.auth

import com.edgareldy.tutorial.domain.repository.PasswordResetRepository
import javax.inject.Inject

/**
 * Step 1 of the mobile password reset flow.
 * Requests a one-time password to be sent to [email].
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
class SendPasswordResetOtpUseCase @Inject constructor(
    private val passwordResetRepository: PasswordResetRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email must not be blank"))
        }
        return passwordResetRepository.sendOtp(email.trim())
    }
}
