package com.edgareldy.tutorial.auth.domain.usecases

import com.edgareldy.tutorial.auth.domain.model.User
import com.edgareldy.tutorial.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Validates the activation code format and delegates account activation to [AuthRepository].
 * Business rule: the activation code must be exactly 6 numeric digits.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
class ActivateAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, activationCode: String): Result<User> {
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email must not be blank"))
        }
        if (!activationCode.matches(Regex("\\d{6}"))) {
            return Result.failure(IllegalArgumentException("Activation code must be 6 digits"))
        }
        return authRepository.activateAccount(email.trim(), activationCode)
    }
}
