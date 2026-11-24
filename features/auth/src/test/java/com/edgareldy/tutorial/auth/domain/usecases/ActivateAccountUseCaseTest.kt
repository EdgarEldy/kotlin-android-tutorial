package com.edgareldy.tutorial.auth.domain.usecases

import com.edgareldy.tutorial.auth.domain.model.User
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
 * Unit tests for ActivateAccountUseCase.
 * Verifies that the activation code must be exactly 6 numeric digits,
 * email must not be blank, and that delegation to AuthRepository occurs
 * only when all inputs pass validation.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
class ActivateAccountUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var activateAccountUseCase: ActivateAccountUseCase

    @Before
    fun setUp() {
        authRepository = mockk()
        activateAccountUseCase = ActivateAccountUseCase(authRepository)
    }

    @Test
    fun _01_ShouldReturnFailure_WhenActivationCodeContainsNonNumericCharacters() = runTest {
        val result = activateAccountUseCase("user@example.com", "abcdef")

        assertTrue(result.isFailure)
        assertEquals("Activation code must be 6 digits", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { authRepository.activateAccount(any(), any()) }
    }

    @Test
    fun _02_ShouldReturnFailure_WhenActivationCodeHasFewerThanSixDigits() = runTest {
        val result = activateAccountUseCase("user@example.com", "12345")

        assertTrue(result.isFailure)
        assertEquals("Activation code must be 6 digits", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { authRepository.activateAccount(any(), any()) }
    }

    @Test
    fun _03_ShouldReturnFailure_WhenActivationCodeHasMoreThanSixDigits() = runTest {
        val result = activateAccountUseCase("user@example.com", "1234567")

        assertTrue(result.isFailure)
        assertEquals("Activation code must be 6 digits", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { authRepository.activateAccount(any(), any()) }
    }

    @Test
    fun _04_ShouldReturnFailure_WhenActivationCodeIsEmpty() = runTest {
        val result = activateAccountUseCase("user@example.com", "")

        assertTrue(result.isFailure)
        assertEquals("Activation code must be 6 digits", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { authRepository.activateAccount(any(), any()) }
    }

    @Test
    fun _05_ShouldReturnFailure_WhenEmailIsBlank() = runTest {
        val result = activateAccountUseCase("", "123456")

        assertTrue(result.isFailure)
        assertEquals("Email must not be blank", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { authRepository.activateAccount(any(), any()) }
    }

    @Test
    fun _06_ShouldDelegateToRepositoryAndReturnSuccess_WhenEmailAndSixDigitCodeAreValid() = runTest {
        val fakeUser = User(1L, "John", "Doe", "johndoe", "user@example.com", true)
        coEvery { authRepository.activateAccount("user@example.com", "123456") } returns
            Result.success(fakeUser)

        val result = activateAccountUseCase("user@example.com", "123456")

        assertTrue(result.isSuccess)
        assertEquals(fakeUser, result.getOrNull())
        coVerify(exactly = 1) { authRepository.activateAccount("user@example.com", "123456") }
    }

    @Test
    fun _07_ShouldPropagateFailure_WhenActivationCodeIsInvalidOnBackend() = runTest {
        val backendError = RuntimeException("Activation code expired")
        coEvery { authRepository.activateAccount(any(), any()) } returns Result.failure(backendError)

        val result = activateAccountUseCase("user@example.com", "123456")

        assertTrue(result.isFailure)
        assertEquals("Activation code expired", result.exceptionOrNull()?.message)
    }
}
