package com.edgareldy.tutorial.data.remote.api

import com.edgareldy.tutorial.data.remote.dto.ActivateAccountRequest
import com.edgareldy.tutorial.data.remote.dto.ApiResponse
import com.edgareldy.tutorial.data.remote.dto.AuthResponseDto
import com.edgareldy.tutorial.data.remote.dto.LoginRequest
import com.edgareldy.tutorial.data.remote.dto.RegisterRequest
import com.edgareldy.tutorial.data.remote.dto.UserResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit service for /api/v1/auth/ endpoints.
 * logout, refreshToken and me pass Authorization explicitly because the
 * AuthInterceptor skips /auth/ paths to avoid attaching an expired token
 * during the login/register/activate flows.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
interface AuthApiService {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<AuthResponseDto>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<UserResponseDto>

    @POST("api/v1/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): ApiResponse<Unit>

    @POST("api/v1/auth/refresh-token")
    suspend fun refreshToken(@Header("Authorization") token: String): ApiResponse<AuthResponseDto>

    @GET("api/v1/auth/me")
    suspend fun getMe(@Header("Authorization") token: String): ApiResponse<UserResponseDto>

    @POST("api/v1/auth/activate-account")
    suspend fun activateAccount(
        @Body request: ActivateAccountRequest
    ): ApiResponse<UserResponseDto>
}
