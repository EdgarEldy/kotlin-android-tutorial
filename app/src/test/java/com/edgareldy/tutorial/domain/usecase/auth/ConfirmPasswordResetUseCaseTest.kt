package com.edgareldy.tutorial.domain.usecase.auth

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
    fun `returns failure and skips network call when new password is blank`() = runTest {
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
    fun `returns failure and skips network call when passwords do not match`() = runTest {
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
    fun `delegates to repository and returns success when otp and matching passwords are valid`() = runTest {
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
    fun `propagates repository failure when OTP is expired on backend`() = runTest {
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
