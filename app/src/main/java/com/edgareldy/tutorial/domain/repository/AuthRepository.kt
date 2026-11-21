package com.edgareldy.tutorial.domain.repository

import com.edgareldy.tutorial.domain.model.AuthTokens
import com.edgareldy.tutorial.domain.model.User

/**
 * Contract for all authentication operations.
 * The domain layer defines this interface; the data layer provides the implementation.
 * All suspend functions return Result<T> so callers can handle success and failure uniformly.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
interface AuthRepository {

    /**
     * Authenticates the user with email and password.
     * On success, the implementation persists the tokens in SecureTokenStorage.
     */
    suspend fun login(email: String, password: String): Result<AuthTokens>

    /**
     * Creates a new user account. The account is inactive until [activateAccount] is called.
     */
    suspend fun register(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Result<User>

    /**
     * Signs out the current user server-side and clears local tokens.
     */
    suspend fun logout(): Result<Unit>

    /**
     * Refreshes the access token using the stored refresh token.
     * On success, the implementation overwrites the tokens in SecureTokenStorage.
     */
    suspend fun refreshToken(): Result<AuthTokens>

    /**
     * Returns the profile of the currently authenticated user.
     */
    suspend fun getMe(): Result<User>

    /**
     * Activates an account by verifying the 6-digit code sent to [email].
     */
    suspend fun activateAccount(email: String, activationCode: String): Result<User>

    /**
     * Returns true if a valid access token is stored locally.
     * This check is synchronous and does not make a network call.
     */
    fun isLoggedIn(): Boolean
}
