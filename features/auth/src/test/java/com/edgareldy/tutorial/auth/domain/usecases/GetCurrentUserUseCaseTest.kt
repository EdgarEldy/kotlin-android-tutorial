package com.edgareldy.tutorial.auth.domain.usecases

import com.edgareldy.tutorial.domain.model.User
import com.edgareldy.tutorial.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetCurrentUserUseCase.
 * Verifies that the use case delegates to AuthRepository.getMe() without
 * any client-side transformation, and propagates both success and failure.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class GetCurrentUserUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase

    private val fakeUser = User(
        id = 1L,
        firstName = "John",
        lastName = "Doe",
        username = "johndoe",
        email = "john@example.com",
        enabled = true
    )

    @Before
    fun setUp() {
        authRepository = mockk()
        getCurrentUserUseCase = GetCurrentUserUseCase(authRepository)
    }

    @Test
    fun _01_ShouldDelegateToRepositoryAndReturnSuccess_WhenUserIsAuthenticated() = runTest {
        coEvery { authRepository.getMe() } returns Result.success(fakeUser)

        val result = getCurrentUserUseCase()

        assertTrue(result.isSuccess)
        assertEquals(fakeUser, result.getOrNull())
        coVerify(exactly = 1) { authRepository.getMe() }
    }

    @Test
    fun _02_ShouldPropagateFailure_WhenNetworkErrorOccurs() = runTest {
        val networkError = RuntimeException("Network unavailable")
        coEvery { authRepository.getMe() } returns Result.failure(networkError)

        val result = getCurrentUserUseCase()

        assertTrue(result.isFailure)
        assertEquals("Network unavailable", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { authRepository.getMe() }
    }

    @Test
    fun _03_ShouldPropagateFailure_WhenTokenIsExpiredOnBackend() = runTest {
        val authError = RuntimeException("Unauthorized")
        coEvery { authRepository.getMe() } returns Result.failure(authError)

        val result = getCurrentUserUseCase()

        assertTrue(result.isFailure)
        assertEquals("Unauthorized", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { authRepository.getMe() }
    }
}
