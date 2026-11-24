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
 * Unit tests for VerifyPasswordResetOtpUseCase.
 * Verifies that both email and OTP must be non-blank before the network call is made,
 * that inputs are trimmed before delegation, and that repository failures are propagated.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class VerifyPasswordResetOtpUseCaseTest {

    private lateinit var passwordResetRepository: PasswordResetRepository
    private lateinit var verifyPasswordResetOtpUseCase: VerifyPasswordResetOtpUseCase

    @Before
    fun setUp() {
        passwordResetRepository = mockk()
        verifyPasswordResetOtpUseCase = VerifyPasswordResetOtpUseCase(passwordResetRepository)
    }

    @Test
    fun _01_ShouldReturnFailureAndSkipNetworkCall_WhenEmailIsBlank() = runTest {
        val result = verifyPasswordResetOtpUseCase("", "123456")

        assertTrue(result.isFailure)
        assertEquals("Email and OTP must not be blank", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { passwordResetRepository.verifyOtp(any(), any()) }
    }

    @Test
    fun _02_ShouldReturnFailureAndSkipNetworkCall_WhenOtpIsBlank() = runTest {
        val result = verifyPasswordResetOtpUseCase("user@example.com", "")

        assertTrue(result.isFailure)
        assertEquals("Email and OTP must not be blank", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { passwordResetRepository.verifyOtp(any(), any()) }
    }

    @Test
    fun _03_ShouldReturnFailureAndSkipNetworkCall_WhenBothInputsAreBlank() = runTest {
        val result = verifyPasswordResetOtpUseCase("", "")

        assertTrue(result.isFailure)
        assertEquals("Email and OTP must not be blank", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { passwordResetRepository.verifyOtp(any(), any()) }
    }

    @Test
    fun _04_ShouldDelegateToRepositoryAndReturnSuccess_WhenEmailAndOtpAreValid() = runTest {
        coEvery {
            passwordResetRepository.verifyOtp("user@example.com", "123456")
        } returns Result.success(Unit)

        val result = verifyPasswordResetOtpUseCase("user@example.com", "123456")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { passwordResetRepository.verifyOtp("user@example.com", "123456") }
    }

    @Test
    fun _05_ShouldTrimBothInputs_WhenDelegatingToRepository() = runTest {
        coEvery {
            passwordResetRepository.verifyOtp("user@example.com", "123456")
        } returns Result.success(Unit)

        val result = verifyPasswordResetOtpUseCase("  user@example.com  ", "  123456  ")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { passwordResetRepository.verifyOtp("user@example.com", "123456") }
    }

    @Test
    fun _06_ShouldPropagateFailure_WhenOtpIsExpiredOrInvalid() = runTest {
        val backendError = RuntimeException("Invalid or expired OTP")
        coEvery {
            passwordResetRepository.verifyOtp(any(), any())
        } returns Result.failure(backendError)

        val result = verifyPasswordResetOtpUseCase("user@example.com", "123456")

        assertTrue(result.isFailure)
        assertEquals("Invalid or expired OTP", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { passwordResetRepository.verifyOtp("user@example.com", "123456") }
    }
}
