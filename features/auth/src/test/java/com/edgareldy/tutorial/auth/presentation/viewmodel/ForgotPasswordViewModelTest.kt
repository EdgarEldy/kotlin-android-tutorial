package com.edgareldy.tutorial.auth.presentation.viewmodel

import app.cash.turbine.test
import com.edgareldy.tutorial.auth.domain.usecases.SendPasswordResetOtpUseCase
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
 * Unit tests for ForgotPasswordViewModel.
 * Verifies the complete UiState emission sequence (Idle -> Loading -> RequestAccepted/Error)
 * driven by SendPasswordResetOtpUseCase results, using Turbine to assert each state transition.
 * RequestAccepted carries the submitted email, which is also verified here.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

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
    fun _01_ShouldBeIdle_WhenNoOtpRequestHasBeenMade() = runTest(testDispatcher) {
        val useCase = mockk<SendPasswordResetOtpUseCase>()
        val viewModel = ForgotPasswordViewModel(useCase)

        viewModel.uiState.test {
            assertEquals(ForgotPasswordUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _02_ShouldEmitLoadingThenRequestAccepted_WhenUseCaseSucceeds() = runTest(testDispatcher) {
        val useCase = mockk<SendPasswordResetOtpUseCase>()
        val viewModel = ForgotPasswordViewModel(useCase)

        coEvery { useCase(any()) } coAnswers {
            delay(100)
            Result.success(Unit)
        }

        viewModel.uiState.test {
            assertEquals(ForgotPasswordUiState.Idle, awaitItem())
            viewModel.sendOtp("user@example.com")
            assertEquals(ForgotPasswordUiState.Loading, awaitItem())
            assertEquals(ForgotPasswordUiState.RequestAccepted("user@example.com"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _03_ShouldExposeEmailInRequestAccepted_WhenOtpIsSent() = runTest(testDispatcher) {
        val useCase = mockk<SendPasswordResetOtpUseCase>()
        val viewModel = ForgotPasswordViewModel(useCase)

        coEvery { useCase(any()) } returns Result.success(Unit)

        viewModel.uiState.test {
            assertEquals(ForgotPasswordUiState.Idle, awaitItem())
            viewModel.sendOtp("specific@email.com")
            assertEquals(ForgotPasswordUiState.Loading, awaitItem())
            val accepted = awaitItem()
            assertTrue(accepted is ForgotPasswordUiState.RequestAccepted)
            assertEquals("specific@email.com", (accepted as ForgotPasswordUiState.RequestAccepted).email)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _04_ShouldEmitLoadingThenError_WhenUseCaseFails() = runTest(testDispatcher) {
        val useCase = mockk<SendPasswordResetOtpUseCase>()
        val viewModel = ForgotPasswordViewModel(useCase)

        coEvery { useCase(any()) } returns Result.failure(RuntimeException("Network error"))

        viewModel.uiState.test {
            assertEquals(ForgotPasswordUiState.Idle, awaitItem())
            viewModel.sendOtp("user@example.com")
            assertEquals(ForgotPasswordUiState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is ForgotPasswordUiState.Error)
            assertEquals("Network error", (error as ForgotPasswordUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _05_ShouldUseFallbackMessage_WhenExceptionHasNoMessage() = runTest(testDispatcher) {
        val useCase = mockk<SendPasswordResetOtpUseCase>()
        val viewModel = ForgotPasswordViewModel(useCase)

        coEvery { useCase(any()) } returns Result.failure(RuntimeException())

        viewModel.uiState.test {
            assertEquals(ForgotPasswordUiState.Idle, awaitItem())
            viewModel.sendOtp("user@example.com")
            assertEquals(ForgotPasswordUiState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is ForgotPasswordUiState.Error)
            assertEquals("Failed to send OTP", (error as ForgotPasswordUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _06_ShouldReturnStateToIdle_WhenResetStateIsCalled() = runTest(testDispatcher) {
        val useCase = mockk<SendPasswordResetOtpUseCase>()
        val viewModel = ForgotPasswordViewModel(useCase)

        coEvery { useCase(any()) } returns Result.failure(RuntimeException("Network error"))

        viewModel.uiState.test {
            assertEquals(ForgotPasswordUiState.Idle, awaitItem())
            viewModel.sendOtp("user@example.com")
            assertEquals(ForgotPasswordUiState.Loading, awaitItem())
            assertTrue(awaitItem() is ForgotPasswordUiState.Error)
            viewModel.resetState()
            assertEquals(ForgotPasswordUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
