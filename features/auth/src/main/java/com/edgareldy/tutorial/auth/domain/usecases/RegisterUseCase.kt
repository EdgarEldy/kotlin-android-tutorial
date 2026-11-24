package com.edgareldy.tutorial.auth.domain.usecases

import com.edgareldy.tutorial.auth.domain.model.User
import com.edgareldy.tutorial.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Validates registration inputs and delegates to [AuthRepository.register].
 * Client-side rule: passwords must match before sending to the backend.
 * The backend enforces password strength and uniqueness constraints.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Result<User> {
        if (firstName.isBlank() || lastName.isBlank() || username.isBlank() || email.isBlank()) {
            return Result.failure(IllegalArgumentException("All fields are required"))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Password must not be blank"))
        }
        if (password != confirmPassword) {
            return Result.failure(IllegalArgumentException("Passwords do not match"))
        }
        return authRepository.register(
            firstName.trim(),
            lastName.trim(),
            username.trim(),
            email.trim(),
            password,
            confirmPassword
        )
    }
}
