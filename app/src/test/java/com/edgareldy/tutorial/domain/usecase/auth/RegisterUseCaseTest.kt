package com.edgareldy.tutorial.domain.usecase.auth

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
 * Unit tests for RegisterUseCase.
 * Verifies that passwords must match, all required fields must be non-blank,
 * and that the use case correctly delegates to AuthRepository on valid input.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
class RegisterUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var registerUseCase: RegisterUseCase

    @Before
    fun setUp() {
        authRepository = mockk()
        registerUseCase = RegisterUseCase(authRepository)
    }

    @Test
    fun `returns failure and skips network call when passwords do not match`() = runTest {
        val result = registerUseCase(
            firstName = "John",
            lastName = "Doe",
            username = "johndoe",
            email = "john@example.com",
            password = "password123",
            confirmPassword = "differentPassword"
        )

        assertTrue(result.isFailure)
        assertEquals("Passwords do not match", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { authRepository.register(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `returns failure and skips network call when first name is blank`() = runTest {
        val result = registerUseCase(
            firstName = "",
            lastName = "Doe",
            username = "johndoe",
            email = "john@example.com",
            password = "password123",
            confirmPassword = "password123"
        )

        assertTrue(result.isFailure)
        assertEquals("All fields are required", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { authRepository.register(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `returns failure and skips network call when password is blank`() = runTest {
        val result = registerUseCase(
            firstName = "John",
            lastName = "Doe",
            username = "johndoe",
            email = "john@example.com",
            password = "",
            confirmPassword = ""
        )

        assertTrue(result.isFailure)
        assertEquals("Password must not be blank", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { authRepository.register(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `delegates to repository and returns success when all fields are valid`() = runTest {
        val fakeUser = User(1L, "John", "Doe", "johndoe", "john@example.com", false)
        coEvery {
            authRepository.register("John", "Doe", "johndoe", "john@example.com", "password123", "password123")
        } returns Result.success(fakeUser)

        val result = registerUseCase(
            firstName = "John",
            lastName = "Doe",
            username = "johndoe",
            email = "john@example.com",
            password = "password123",
            confirmPassword = "password123"
        )

        assertTrue(result.isSuccess)
        assertEquals(fakeUser, result.getOrNull())
        coVerify(exactly = 1) {
            authRepository.register("John", "Doe", "johndoe", "john@example.com", "password123", "password123")
        }
    }

    @Test
    fun `propagates repository failure when backend rejects registration`() = runTest {
        val backendError = RuntimeException("Email already in use")
        coEvery {
            authRepository.register(any(), any(), any(), any(), any(), any())
        } returns Result.failure(backendError)

        val result = registerUseCase(
            firstName = "John",
            lastName = "Doe",
            username = "johndoe",
            email = "john@example.com",
            password = "password123",
            confirmPassword = "password123"
        )

        assertTrue(result.isFailure)
        assertEquals("Email already in use", result.exceptionOrNull()?.message)
    }
}
