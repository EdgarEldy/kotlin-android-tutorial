package com.edgareldy.tutorial.auth.domain.usecases

import com.edgareldy.tutorial.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for LogoutUseCase.
 * LogoutUseCase delegates entirely to AuthRepository.logout(), which is
 * responsible for invalidating the server-side session and clearing local tokens.
 * The token clearing behavior regardless of network outcome is verified in
 * AuthRepositoryImplTest. These tests focus on correct delegation and result propagation.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
class LogoutUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var logoutUseCase: LogoutUseCase

    @Before
    fun setUp() {
        authRepository = mockk()
        logoutUseCase = LogoutUseCase(authRepository)
    }

    @Test
    fun _01_ShouldDelegateToRepositoryAndReturnSuccess_WhenLogoutSucceeds() = runTest {
        coEvery { authRepository.logout() } returns Result.success(Unit)

        val result = logoutUseCase()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { authRepository.logout() }
    }

    @Test
    fun _02_ShouldDelegateToRepositoryAndPropagateFailure_WhenLogoutFails() = runTest {
        val networkError = RuntimeException("Network unavailable")
        coEvery { authRepository.logout() } returns Result.failure(networkError)

        val result = logoutUseCase()

        assertTrue(result.isFailure)
        coVerify(exactly = 1) { authRepository.logout() }
    }

    @Test
    fun _03_ShouldAlwaysCallRepository_SoThatTokensAreClearedRegardlessOfNetwork() = runTest {
        // AuthRepositoryImpl.logout() always clears tokens even when the network call fails.
        // This test ensures the use case always delegates, triggering that cleanup path.
        coEvery { authRepository.logout() } returns Result.success(Unit)

        logoutUseCase()

        coVerify(exactly = 1) { authRepository.logout() }
    }
}
