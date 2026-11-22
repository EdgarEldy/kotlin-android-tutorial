package com.edgareldy.tutorial.features.auth.viewmodel

import com.edgareldy.tutorial.domain.usecase.auth.RegisterUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for RegisterViewModel.
 * Verifies UiState transitions (Idle, Loading, Success, Error) driven by
 * RegisterUseCase results, that Success carries the registered email, and
 * that the resetState action restores Idle.
 * Uses UnconfinedTestDispatcher so viewModelScope.launch runs eagerly.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun registerArgs(
        firstName: String = "John",
        lastName: String = "Doe",
        username: String = "johndoe",
        email: String = "john@example.com",
        password: String = "password123",
        confirmPassword: String = "password123"
    ) = arrayOf(firstName, lastName, username, email, password, confirmPassword)

    @Test
    fun _01_ShouldBeIdle_WhenNoRegistrationAttemptHasBeenMade() = runTest(testDispatcher) {
        val useCase = mockk<RegisterUseCase>()
        val viewModel = RegisterViewModel(useCase)

        assertEquals(RegisterUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun _02_ShouldSetStateToLoading_WhileUseCaseExecutes() = runTest(testDispatcher) {
        val useCase = mockk<RegisterUseCase>()
        val viewModel = RegisterViewModel(useCase)

        // delay forces a real suspension point so Loading is observable before Success
        coEvery { useCase(any(), any(), any(), any(), any(), any()) } coAnswers {
            delay(1_000)
            Result.success(Unit)
        }

        viewModel.register(*registerArgs())

        assertEquals(RegisterUiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()
        assertEquals(RegisterUiState.Success("john@example.com"), viewModel.uiState.value)
    }

    @Test
    fun _03_ShouldUpdateStateToSuccess_WhenRegistrationSucceeds() = runTest(testDispatcher) {
        val useCase = mockk<RegisterUseCase>()
        val viewModel = RegisterViewModel(useCase)
        coEvery { useCase(any(), any(), any(), any(), any(), any()) } returns Result.success(Unit)

        viewModel.register(*registerArgs())
        advanceUntilIdle()

        assertEquals(RegisterUiState.Success("john@example.com"), viewModel.uiState.value)
    }

    @Test
    fun _04_ShouldExposeRegisteredEmail_InSuccessState() = runTest(testDispatcher) {
        val useCase = mockk<RegisterUseCase>()
        val viewModel = RegisterViewModel(useCase)
        coEvery { useCase(any(), any(), any(), any(), any(), any()) } returns Result.success(Unit)

        viewModel.register(*registerArgs(email = "custom@test.org"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is RegisterUiState.Success)
        assertEquals("custom@test.org", (state as RegisterUiState.Success).email)
    }

    @Test
    fun _05_ShouldUpdateStateToError_WhenRegistrationFails() = runTest(testDispatcher) {
        val useCase = mockk<RegisterUseCase>()
        val viewModel = RegisterViewModel(useCase)
        coEvery { useCase(any(), any(), any(), any(), any(), any()) } returns
            Result.failure(RuntimeException("Email already in use"))

        viewModel.register(*registerArgs())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is RegisterUiState.Error)
        assertEquals("Email already in use", (state as RegisterUiState.Error).message)
    }

    @Test
    fun _06_ShouldUseFallbackMessage_WhenExceptionHasNoMessage() = runTest(testDispatcher) {
        val useCase = mockk<RegisterUseCase>()
        val viewModel = RegisterViewModel(useCase)
        coEvery { useCase(any(), any(), any(), any(), any(), any()) } returns
            Result.failure(RuntimeException())

        viewModel.register(*registerArgs())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is RegisterUiState.Error)
        assertEquals("Registration failed", (state as RegisterUiState.Error).message)
    }

    @Test
    fun _07_ShouldReturnStateToIdle_WhenResetStateIsCalled() = runTest(testDispatcher) {
        val useCase = mockk<RegisterUseCase>()
        val viewModel = RegisterViewModel(useCase)
        coEvery { useCase(any(), any(), any(), any(), any(), any()) } returns
            Result.failure(RuntimeException("Network error"))
        viewModel.register(*registerArgs())
        advanceUntilIdle()

        viewModel.resetState()

        assertEquals(RegisterUiState.Idle, viewModel.uiState.value)
    }
}
