package com.edgareldy.tutorial.auth.presentation.viewmodel

import app.cash.turbine.test
import com.edgareldy.tutorial.auth.domain.usecases.RegisterUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
 * Verifies the complete UiState emission sequence (Idle -> Loading -> Success/Error)
 * driven by RegisterUseCase results, using Turbine to assert each state transition.
 * Success state carries the registered email, which is also verified here.
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

        viewModel.uiState.test {
            assertEquals(RegisterUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _02_ShouldEmitLoadingThenSuccess_WhenRegistrationSucceeds() = runTest(testDispatcher) {
        val useCase = mockk<RegisterUseCase>()
        val viewModel = RegisterViewModel(useCase)

        coEvery { useCase(any(), any(), any(), any(), any(), any()) } coAnswers {
            delay(100)
            Result.success(Unit)
        }

        viewModel.uiState.test {
            assertEquals(RegisterUiState.Idle, awaitItem())
            viewModel.register(*registerArgs())
            assertEquals(RegisterUiState.Loading, awaitItem())
            assertEquals(RegisterUiState.Success("john@example.com"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _03_ShouldExposeRegisteredEmail_InSuccessState() = runTest(testDispatcher) {
        val useCase = mockk<RegisterUseCase>()
        val viewModel = RegisterViewModel(useCase)

        coEvery { useCase(any(), any(), any(), any(), any(), any()) } returns Result.success(Unit)

        viewModel.uiState.test {
            assertEquals(RegisterUiState.Idle, awaitItem())
            viewModel.register(*registerArgs(email = "custom@test.org"))
            assertEquals(RegisterUiState.Loading, awaitItem())
            val success = awaitItem()
            assertTrue(success is RegisterUiState.Success)
            assertEquals("custom@test.org", (success as RegisterUiState.Success).email)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _04_ShouldEmitLoadingThenError_WhenRegistrationFails() = runTest(testDispatcher) {
        val useCase = mockk<RegisterUseCase>()
        val viewModel = RegisterViewModel(useCase)

        coEvery { useCase(any(), any(), any(), any(), any(), any()) } returns
            Result.failure(RuntimeException("Email already in use"))

        viewModel.uiState.test {
            assertEquals(RegisterUiState.Idle, awaitItem())
            viewModel.register(*registerArgs())
            assertEquals(RegisterUiState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is RegisterUiState.Error)
            assertEquals("Email already in use", (error as RegisterUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _05_ShouldUseFallbackMessage_WhenExceptionHasNoMessage() = runTest(testDispatcher) {
        val useCase = mockk<RegisterUseCase>()
        val viewModel = RegisterViewModel(useCase)

        coEvery { useCase(any(), any(), any(), any(), any(), any()) } returns
            Result.failure(RuntimeException())

        viewModel.uiState.test {
            assertEquals(RegisterUiState.Idle, awaitItem())
            viewModel.register(*registerArgs())
            assertEquals(RegisterUiState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is RegisterUiState.Error)
            assertEquals("Registration failed", (error as RegisterUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _06_ShouldReturnStateToIdle_WhenResetStateIsCalled() = runTest(testDispatcher) {
        val useCase = mockk<RegisterUseCase>()
        val viewModel = RegisterViewModel(useCase)

        coEvery { useCase(any(), any(), any(), any(), any(), any()) } returns
            Result.failure(RuntimeException("Network error"))

        viewModel.uiState.test {
            assertEquals(RegisterUiState.Idle, awaitItem())
            viewModel.register(*registerArgs())
            assertEquals(RegisterUiState.Loading, awaitItem())
            assertTrue(awaitItem() is RegisterUiState.Error)
            viewModel.resetState()
            assertEquals(RegisterUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
