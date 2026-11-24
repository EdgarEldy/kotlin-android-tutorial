package com.edgareldy.tutorial.auth.domain.usecases

import com.edgareldy.tutorial.auth.domain.model.AuthTokens
import com.edgareldy.tutorial.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for LoginUseCase.
 * Verifies input validation (blank email, blank password) and delegation
 * to AuthRepository when inputs are valid.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
class SignInUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setUp() {
        authRepository = mockk()
        loginUseCase = LoginUseCase(authRepository)
    }

    @Test
    fun _01_ShouldReturnFailure_WhenEmailIsBlank() = runTest {
        val result = loginUseCase("", "password123")

        assertTrue(result.isFailure)
        assertEquals("Email must not be blank", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    @Test
    fun _02_ShouldReturnFailure_WhenPasswordIsBlank() = runTest {
        val result = loginUseCase("user@example.com", "")

        assertTrue(result.isFailure)
        assertEquals("Password must not be blank", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    @Test
    fun _03_ShouldDelegateToRepositoryAndReturnSuccess_WhenCredentialsAreValid() = runTest {
        val fakeTokens = AuthTokens("access_token", "refresh_token", 3600L)
        coEvery { authRepository.login("user@example.com", "password123") } returns
            Result.success(fakeTokens)

        val result = loginUseCase("user@example.com", "password123")

        assertTrue(result.isSuccess)
        assertEquals(fakeTokens, result.getOrNull())
        coVerify(exactly = 1) { authRepository.login("user@example.com", "password123") }
    }

    @Test
    fun _04_ShouldTrimEmailWhitespace_WhenDelegatingToRepository() = runTest {
        val fakeTokens = AuthTokens("access_token", "refresh_token", 3600L)
        coEvery { authRepository.login("user@example.com", "password123") } returns
            Result.success(fakeTokens)

        val result = loginUseCase("  user@example.com  ", "password123")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { authRepository.login("user@example.com", "password123") }
    }

    @Test
    fun _05_ShouldPropagateFailure_WhenNetworkErrorOccurs() = runTest {
        val networkError = RuntimeException("Network unavailable")
        coEvery { authRepository.login(any(), any()) } returns Result.failure(networkError)

        val result = loginUseCase("user@example.com", "password123")

        assertTrue(result.isFailure)
        assertEquals("Network unavailable", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { authRepository.login("user@example.com", "password123") }
    }
}
