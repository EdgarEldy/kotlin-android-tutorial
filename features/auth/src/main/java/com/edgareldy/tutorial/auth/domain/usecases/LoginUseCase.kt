package com.edgareldy.tutorial.auth.domain.usecases

import com.edgareldy.tutorial.auth.domain.model.AuthTokens
import com.edgareldy.tutorial.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Validates login inputs and delegates to [AuthRepository.login].
 * Business rule: email must not be blank before making a network call.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthTokens> {
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email must not be blank"))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Password must not be blank"))
        }
        return authRepository.login(email.trim(), password)
    }
}
