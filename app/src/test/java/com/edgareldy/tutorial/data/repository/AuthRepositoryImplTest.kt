package com.edgareldy.tutorial.data.repository

import com.edgareldy.tutorial.core.storage.SecureTokenStorage
import com.edgareldy.tutorial.data.remote.api.AuthApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

/**
 * Integration tests for AuthRepositoryImpl using MockWebServer.
 * SecureTokenStorage is mocked because it requires the Android Keystore.
 * These tests verify the mapping from HTTP responses to domain models,
 * the token-persistence side-effects on login, and the token-clearing
 * behavior of logout regardless of network outcome.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
class AuthRepositoryImplTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var authApiService: AuthApiService
    private lateinit var tokenStorage: SecureTokenStorage
    private lateinit var repository: AuthRepositoryImpl

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        authApiService = retrofit.create(AuthApiService::class.java)
        tokenStorage = mockk(relaxed = true)
        repository = AuthRepositoryImpl(authApiService, tokenStorage)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun _01_ShouldSaveTokensAndReturnAuthTokens_WhenLoginResponseIs200() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "success": true,
                        "message": "Login successful",
                        "data": {
                            "access_token": "mock_access_token",
                            "token_type": "Bearer",
                            "refresh_token": "mock_refresh_token",
                            "expires_in": 3600
                        },
                        "timestamp": "2026-11-21T10:00:00"
                    }
                    """.trimIndent()
                )
        )

        val result = repository.login("user@example.com", "password123")

        assertTrue(result.isSuccess)
        val tokens = result.getOrNull()!!
        assertEquals("mock_access_token", tokens.accessToken)
        assertEquals("mock_refresh_token", tokens.refreshToken)
        assertEquals(3600L, tokens.expiresIn)
        verify(exactly = 1) { tokenStorage.saveTokens("mock_access_token", "mock_refresh_token") }
    }

    @Test
    fun _02_ShouldReturnFailureWithoutSavingTokens_WhenLoginResponseIs401() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        val result = repository.login("user@example.com", "wrong_password")

        assertTrue(result.isFailure)
        verify(exactly = 0) { tokenStorage.saveTokens(any(), any()) }
    }

    @Test
    fun _03_ShouldReturnFailureWithoutSavingTokens_WhenLoginResponseIs500() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        val result = repository.login("user@example.com", "password123")

        assertTrue(result.isFailure)
        verify(exactly = 0) { tokenStorage.saveTokens(any(), any()) }
    }

    @Test
    fun _04_ShouldReturnMappedUserDomainModel_WhenGetMeResponseIs200() = runTest {
        every { tokenStorage.getAccessToken() } returns "valid_access_token"

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "success": true,
                        "message": "Profile retrieved",
                        "data": {
                            "id": 42,
                            "first_name": "Jane",
                            "last_name": "Smith",
                            "username": "janesmith",
                            "email": "jane@example.com",
                            "enabled": true,
                            "account_locked": false
                        },
                        "timestamp": "2026-11-21T10:00:00"
                    }
                    """.trimIndent()
                )
        )

        val result = repository.getMe()

        assertTrue(result.isSuccess)
        val user = result.getOrNull()!!
        assertEquals(42L, user.id)
        assertEquals("Jane", user.firstName)
        assertEquals("Smith", user.lastName)
        assertEquals("janesmith", user.username)
        assertEquals("jane@example.com", user.email)
        assertTrue(user.enabled)
    }

    @Test
    fun _05_ShouldReturnFailure_WhenNoAccessTokenIsStored() = runTest {
        every { tokenStorage.getAccessToken() } returns null

        val result = repository.getMe()

        assertTrue(result.isFailure)
        assertEquals("Not authenticated", result.exceptionOrNull()?.message)
    }

    @Test
    fun _06_ShouldClearTokensAndReturnSuccess_WhenLogoutNetworkCallSucceeds() = runTest {
        every { tokenStorage.getAccessToken() } returns "valid_access_token"
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"success":true,"message":"Logged out","data":null,"timestamp":"2026-11-21T10:00:00"}""")
        )

        val result = repository.logout()

        assertTrue(result.isSuccess)
        verify(exactly = 1) { tokenStorage.clearTokens() }
    }

    @Test
    fun _07_ShouldClearTokens_EvenWhenLogoutNetworkCallReturnsServerError() = runTest {
        every { tokenStorage.getAccessToken() } returns "valid_access_token"
        // A 503 causes HttpException inside the inner runCatching, which is silently swallowed.
        // clearTokens() is still called.
        mockWebServer.enqueue(MockResponse().setResponseCode(503))

        val result = repository.logout()

        assertTrue(result.isSuccess)
        verify(exactly = 1) { tokenStorage.clearTokens() }
    }

    @Test
    fun _08_ShouldClearTokensAndSkipNetworkCall_WhenNoAccessTokenIsStored() = runTest {
        every { tokenStorage.getAccessToken() } returns null

        val result = repository.logout()

        assertTrue(result.isSuccess)
        verify(exactly = 1) { tokenStorage.clearTokens() }
        assertEquals(0, mockWebServer.requestCount)
    }
}
