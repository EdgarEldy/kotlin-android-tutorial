package com.edgareldy.tutorial.domain.usecases.auth

import com.edgareldy.tutorial.domain.repository.PasswordResetRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SendPasswordResetOtpUseCase.
 * Verifies that a blank email is rejected before the network call, that the email
 * is trimmed before delegation, and that repository failures are propagated.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class SendPasswordResetOtpUseCaseTest {

    private lateinit var passwordResetRepository: PasswordResetRepository
    private lateinit var sendPasswordResetOtpUseCase: SendPasswordResetOtpUseCase

    @Before
    fun setUp() {
        passwordResetRepository = mockk()
        sendPasswordResetOtpUseCase = SendPasswordResetOtpUseCase(passwordResetRepository)
    }

    @Test
    fun _01_ShouldReturnFailureAndSkipNetworkCall_WhenEmailIsBlank() = runTest {
        val result = sendPasswordResetOtpUseCase("")

        assertTrue(result.isFailure)
        assertEquals("Email must not be blank", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { passwordResetRepository.sendOtp(any()) }
    }

    @Test
    fun _02_ShouldReturnFailureAndSkipNetworkCall_WhenEmailIsOnlyWhitespace() = runTest {
        val result = sendPasswordResetOtpUseCase("   ")

        assertTrue(result.isFailure)
        assertEquals("Email must not be blank", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { passwordResetRepository.sendOtp(any()) }
    }

    @Test
    fun _03_ShouldDelegateToRepositoryAndReturnSuccess_WhenEmailIsValid() = runTest {
        coEvery { passwordResetRepository.sendOtp("user@example.com") } returns Result.success(Unit)

        val result = sendPasswordResetOtpUseCase("user@example.com")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { passwordResetRepository.sendOtp("user@example.com") }
    }

    @Test
    fun _04_ShouldTrimEmailWhitespace_WhenDelegatingToRepository() = runTest {
        coEvery { passwordResetRepository.sendOtp("user@example.com") } returns Result.success(Unit)

        val result = sendPasswordResetOtpUseCase("  user@example.com  ")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { passwordResetRepository.sendOtp("user@example.com") }
    }

    @Test
    fun _05_ShouldPropagateFailure_WhenNetworkErrorOccurs() = runTest {
        val networkError = RuntimeException("Network unavailable")
        coEvery { passwordResetRepository.sendOtp(any()) } returns Result.failure(networkError)

        val result = sendPasswordResetOtpUseCase("user@example.com")

        assertTrue(result.isFailure)
        assertEquals("Network unavailable", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { passwordResetRepository.sendOtp("user@example.com") }
    }
}
