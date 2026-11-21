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
    fun `initial state is Idle before any sign-in attempt`() = runTest(testDispatcher) {
        val loginUseCase = mockk<LoginUseCase>()
        val viewModel = SignInViewModel(loginUseCase)

        assertEquals(SignInUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `signIn sets state to Loading while use case executes`() = runTest(testDispatcher) {
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
    fun `signIn updates state to Success when login use case returns success`() = runTest(testDispatcher) {
        val loginUseCase = mockk<LoginUseCase>()
        val viewModel = SignInViewModel(loginUseCase)
        val fakeTokens = AuthTokens("access_token", "refresh_token", 3600L)
        coEvery { loginUseCase(any(), any()) } returns Result.success(fakeTokens)

        viewModel.signIn("user@example.com", "password123")
        advanceUntilIdle()

        assertEquals(SignInUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `signIn updates state to Error when login use case returns failure`() = runTest(testDispatcher) {
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
    fun `signIn propagates validation error message from LoginUseCase`() = runTest(testDispatcher) {
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
    fun `resetState returns state to Idle after a previous sign-in result`() = runTest(testDispatcher) {
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
