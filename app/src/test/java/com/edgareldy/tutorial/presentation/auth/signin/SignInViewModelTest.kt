package com.edgareldy.tutorial.presentation.auth.signin

import com.edgareldy.tutorial.domain.model.AuthTokens
import com.edgareldy.tutorial.domain.usecase.auth.LoginUseCase
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
 * Unit tests for SignInViewModel.
 * Verifies UiState transitions (Idle, Loading, Success, Error) driven by
 * LoginUseCase results, and the resetState action.
 * Uses UnconfinedTestDispatcher so viewModelScope.launch runs eagerly,
 * and TestCoroutineScheduler (via runTest) to control virtual time for
 * the Loading-state assertion.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SignInViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

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

        assertEquals(SignInUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun _02_ShouldSetStateToLoading_WhileUseCaseExecutes() = runTest(testDispatcher) {
        val loginUseCase = mockk<LoginUseCase>()
        val viewModel = SignInViewModel(loginUseCase)

        // delay forces a real suspension point so Loading is observable before Success
        coEvery { loginUseCase(any(), any()) } coAnswers {
            delay(1_000)
            Result.success(AuthTokens("access", "refresh", 3600L))
        }

        viewModel.signIn("user@example.com", "password123")

        assertEquals(SignInUiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()
        assertEquals(SignInUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun _03_ShouldUpdateStateToSuccess_WhenLoginUseCaseReturnsSuccess() = runTest(testDispatcher) {
        val loginUseCase = mockk<LoginUseCase>()
        val viewModel = SignInViewModel(loginUseCase)
        val fakeTokens = AuthTokens("access_token", "refresh_token", 3600L)
        coEvery { loginUseCase(any(), any()) } returns Result.success(fakeTokens)

        viewModel.signIn("user@example.com", "password123")
        advanceUntilIdle()

        assertEquals(SignInUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun _04_ShouldUpdateStateToError_WhenLoginUseCaseReturnsFailure() = runTest(testDispatcher) {
        val loginUseCase = mockk<LoginUseCase>()
        val viewModel = SignInViewModel(loginUseCase)
        coEvery { loginUseCase(any(), any()) } returns
            Result.failure(RuntimeException("Invalid credentials"))

        viewModel.signIn("user@example.com", "wrong_password")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is SignInUiState.Error)
        assertEquals("Invalid credentials", (state as SignInUiState.Error).message)
    }

    @Test
    fun _05_ShouldPropagateValidationErrorMessage_WhenLoginUseCaseReturnsValidationError() = runTest(testDispatcher) {
        val loginUseCase = mockk<LoginUseCase>()
        val viewModel = SignInViewModel(loginUseCase)
        coEvery { loginUseCase("", any()) } returns
            Result.failure(IllegalArgumentException("Email must not be blank"))

        viewModel.signIn("", "password123")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is SignInUiState.Error)
        assertEquals("Email must not be blank", (state as SignInUiState.Error).message)
    }

    @Test
    fun _06_ShouldReturnStateToIdle_WhenResetStateIsCalled() = runTest(testDispatcher) {
        val loginUseCase = mockk<LoginUseCase>()
        val viewModel = SignInViewModel(loginUseCase)
        coEvery { loginUseCase(any(), any()) } returns
            Result.failure(RuntimeException("Network error"))
        viewModel.signIn("user@example.com", "password123")
        advanceUntilIdle()

        viewModel.resetState()

        assertEquals(SignInUiState.Idle, viewModel.uiState.value)
    }
}
