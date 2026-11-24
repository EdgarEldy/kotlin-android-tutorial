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
 * Unit tests for ConfirmPasswordResetUseCase.
 * Verifies the step-3 mobile password reset rules: new password must not be blank
 * and both password fields must match before the repository is called.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
class ConfirmPasswordResetUseCaseTest {

    private lateinit var passwordResetRepository: PasswordResetRepository
    private lateinit var confirmPasswordResetUseCase: ConfirmPasswordResetUseCase

    @Before
    fun setUp() {
        passwordResetRepository = mockk()
        confirmPasswordResetUseCase = ConfirmPasswordResetUseCase(passwordResetRepository)
    }

    @Test
    fun _01_ShouldReturnFailureAndSkipNetworkCall_WhenNewPasswordIsBlank() = runTest {
        val result = confirmPasswordResetUseCase(
            otp = "123456",
            newPassword = "",
            confirmPassword = ""
        )

        assertTrue(result.isFailure)
        assertEquals("Password must not be blank", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { passwordResetRepository.confirmReset(any(), any(), any()) }
    }

    @Test
    fun _02_ShouldReturnFailureAndSkipNetworkCall_WhenPasswordsDoNotMatch() = runTest {
        val result = confirmPasswordResetUseCase(
            otp = "123456",
            newPassword = "newPassword1",
            confirmPassword = "newPassword2"
        )

        assertTrue(result.isFailure)
        assertEquals("Passwords do not match", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { passwordResetRepository.confirmReset(any(), any(), any()) }
    }

    @Test
    fun _03_ShouldDelegateToRepositoryAndReturnSuccess_WhenOtpAndMatchingPasswordsAreValid() = runTest {
        coEvery {
            passwordResetRepository.confirmReset("123456", "newPassword1", "newPassword1")
        } returns Result.success(Unit)

        val result = confirmPasswordResetUseCase(
            otp = "123456",
            newPassword = "newPassword1",
            confirmPassword = "newPassword1"
        )

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            passwordResetRepository.confirmReset("123456", "newPassword1", "newPassword1")
        }
    }

    @Test
    fun _04_ShouldPropagateFailure_WhenOtpIsExpiredOnBackend() = runTest {
        val backendError = RuntimeException("Token expired")
        coEvery {
            passwordResetRepository.confirmReset(any(), any(), any())
        } returns Result.failure(backendError)

        val result = confirmPasswordResetUseCase(
            otp = "123456",
            newPassword = "newPassword1",
            confirmPassword = "newPassword1"
        )

        assertTrue(result.isFailure)
        assertEquals("Token expired", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { passwordResetRepository.confirmReset(any(), any(), any()) }
    }
}
