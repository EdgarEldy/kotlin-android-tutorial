package com.edgareldy.tutorial.data.repository

import com.edgareldy.tutorial.core.storage.SecureTokenStorage
import com.edgareldy.tutorial.data.remote.api.AuthApiService
import com.edgareldy.tutorial.data.remote.dto.ActivateAccountRequest
import com.edgareldy.tutorial.data.remote.dto.LoginRequest
import com.edgareldy.tutorial.data.remote.dto.RegisterRequest
import com.edgareldy.tutorial.domain.model.AuthTokens
import com.edgareldy.tutorial.domain.model.User
import com.edgareldy.tutorial.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [AuthRepository] backed by [AuthApiService].
 * Persists JWT tokens in [SecureTokenStorage] after every successful login or refresh.
 * Maps Retrofit DTOs to domain models at this boundary so the domain stays framework-free.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenStorage: SecureTokenStorage
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthTokens> =
        runCatching {
            val response = authApiService.login(LoginRequest(email, password))
            val dto = requireNotNull(response.data) { response.message ?: "Login failed" }
            val tokens = dto.toDomain()
            tokenStorage.saveTokens(tokens.accessToken, tokens.refreshToken)
            tokens
        }

    override suspend fun register(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Result<User> = runCatching {
        val response = authApiService.register(
            RegisterRequest(firstName, lastName, username, email, password, confirmPassword)
        )
        val dto = requireNotNull(response.data) { response.message ?: "Registration failed" }
        dto.toDomain()
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        val token = tokenStorage.getAccessToken()
        if (token != null) {
            runCatching {
                authApiService.logout("Bearer $token")
            }
        }
        tokenStorage.clearTokens()
    }

    override suspend fun refreshToken(): Result<AuthTokens> = runCatching {
        val refreshToken = requireNotNull(tokenStorage.getRefreshToken()) {
            "No refresh token available"
        }
        val response = authApiService.refreshToken("Bearer $refreshToken")
        val dto = requireNotNull(response.data) { response.message ?: "Token refresh failed" }
        val tokens = dto.toDomain()
        tokenStorage.saveTokens(tokens.accessToken, tokens.refreshToken)
        tokens
    }

    override suspend fun getMe(): Result<User> = runCatching {
        val token = requireNotNull(tokenStorage.getAccessToken()) { "Not authenticated" }
        val response = authApiService.getMe("Bearer $token")
        val dto = requireNotNull(response.data) { response.message ?: "Failed to fetch profile" }
        dto.toDomain()
    }

    override suspend fun activateAccount(email: String, activationCode: String): Result<User> =
        runCatching {
            val response = authApiService.activateAccount(
                ActivateAccountRequest(email, activationCode)
            )
            val dto = requireNotNull(response.data) {
                response.message ?: "Account activation failed"
            }
            dto.toDomain()
        }

    override fun isLoggedIn(): Boolean = tokenStorage.getAccessToken() != null

    // --- Mappers (private, stay within this class to avoid a separate mapper file) ---

    private fun com.edgareldy.tutorial.data.remote.dto.AuthResponseDto.toDomain() = AuthTokens(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresIn = expiresIn
    )

    private fun com.edgareldy.tutorial.data.remote.dto.UserResponseDto.toDomain() = User(
        id = id,
        firstName = firstName.orEmpty(),
        lastName = lastName.orEmpty(),
        username = username.orEmpty(),
        email = email.orEmpty(),
        enabled = enabled
    )
}
