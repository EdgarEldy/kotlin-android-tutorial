package com.edgareldy.tutorial.features.auth.viewmodel

import app.cash.turbine.test
import com.edgareldy.tutorial.domain.model.AuthTokens
import com.edgareldy.tutorial.domain.usecases.auth.LoginUseCase
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
 * Unit tests for SignInViewModel.
 * Verifies the complete UiState emission sequence (Idle -> Loading -> Success/Error)
 * driven by LoginUseCase results, using Turbine to assert each state transition in order.
 * Each test opens a Turbine collector on uiState and captures items with awaitItem().
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SignInViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val fakeTokens = AuthTokens("access_token", "refresh_token", 3600L)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun _01_ShouldBeIdle_WhenNoSignInAttemptHasBeenMade() = runTest(testDispatcher) {
        val loginUseCase = mockk<LoginUseCase>()
        val viewModel = SignInViewModel(loginUseCase)

        viewModel.uiState.test {
            assertEquals(SignInUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _02_ShouldEmitLoadingThenSuccess_WhenCredentialsAreValid() = runTest(testDispatcher) {
        val loginUseCase = mockk<LoginUseCase>()
        val viewModel = SignInViewModel(loginUseCase)

        coEvery { loginUseCase(any(), any()) } coAnswers {
            delay(100)
            Result.success(fakeTokens)
        }

        viewModel.uiState.test {
            assertEquals(SignInUiState.Idle, awaitItem())
            viewModel.signIn("user@example.com", "password123")
            assertEquals(SignInUiState.Loading, awaitItem())
            assertEquals(SignInUiState.Success, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _03_ShouldEmitLoadingThenSuccess_WhenLoginUseCaseReturnsSuccess() = runTest(testDispatcher) {
        val loginUseCase = mockk<LoginUseCase>()
        val viewModel = SignInViewModel(loginUseCase)

        coEvery { loginUseCase(any(), any()) } returns Result.success(fakeTokens)

        viewModel.uiState.test {
            assertEquals(SignInUiState.Idle, awaitItem())
            viewModel.signIn("user@example.com", "password123")
            assertEquals(SignInUiState.Loading, awaitItem())
            assertEquals(SignInUiState.Success, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _04_ShouldEmitLoadingThenError_WhenLoginUseCaseReturnsFailure() = runTest(testDispatcher) {
        val loginUseCase = mockk<LoginUseCase>()
        val viewModel = SignInViewModel(loginUseCase)

        coEvery { loginUseCase(any(), any()) } returns
            Result.failure(RuntimeException("Invalid credentials"))

        viewModel.uiState.test {
            assertEquals(SignInUiState.Idle, awaitItem())
            viewModel.signIn("user@example.com", "wrong_password")
            assertEquals(SignInUiState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is SignInUiState.Error)
            assertEquals("Invalid credentials", (error as SignInUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _05_ShouldPropagateValidationErrorMessage_WhenEmailIsBlank() = runTest(testDispatcher) {
        val loginUseCase = mockk<LoginUseCase>()
        val viewModel = SignInViewModel(loginUseCase)

        coEvery { loginUseCase("", any()) } returns
            Result.failure(IllegalArgumentException("Email must not be blank"))

        viewModel.uiState.test {
            assertEquals(SignInUiState.Idle, awaitItem())
            viewModel.signIn("", "password123")
            assertEquals(SignInUiState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is SignInUiState.Error)
            assertEquals("Email must not be blank", (error as SignInUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _06_ShouldReturnStateToIdle_WhenResetStateIsCalled() = runTest(testDispatcher) {
        val loginUseCase = mockk<LoginUseCase>()
        val viewModel = SignInViewModel(loginUseCase)

        coEvery { loginUseCase(any(), any()) } returns
            Result.failure(RuntimeException("Network error"))

        viewModel.uiState.test {
            assertEquals(SignInUiState.Idle, awaitItem())
            viewModel.signIn("user@example.com", "password123")
            assertEquals(SignInUiState.Loading, awaitItem())
            assertTrue(awaitItem() is SignInUiState.Error)
            viewModel.resetState()
            assertEquals(SignInUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
