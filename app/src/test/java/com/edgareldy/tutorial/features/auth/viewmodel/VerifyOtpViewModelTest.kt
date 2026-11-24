package com.edgareldy.tutorial.features.auth.viewmodel

import app.cash.turbine.test
import com.edgareldy.tutorial.domain.usecases.auth.VerifyPasswordResetOtpUseCase
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
 * Unit tests for VerifyOtpViewModel.
 * Verifies the complete UiState emission sequence (Idle -> Loading -> Success/Error)
 * driven by VerifyPasswordResetOtpUseCase results, using Turbine to assert each transition.
 * Success carries the verified OTP value forwarded to the ResetPassword screen.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VerifyOtpViewModelTest {

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
    fun _01_ShouldBeIdle_WhenNoVerificationAttemptHasBeenMade() = runTest(testDispatcher) {
        val useCase = mockk<VerifyPasswordResetOtpUseCase>()
        val viewModel = VerifyOtpViewModel(useCase)

        viewModel.uiState.test {
            assertEquals(VerifyOtpUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _02_ShouldEmitLoadingThenSuccess_WhenOtpIsVerified() = runTest(testDispatcher) {
        val useCase = mockk<VerifyPasswordResetOtpUseCase>()
        val viewModel = VerifyOtpViewModel(useCase)

        coEvery { useCase(any(), any()) } coAnswers {
            delay(100)
            Result.success(Unit)
        }

        viewModel.uiState.test {
            assertEquals(VerifyOtpUiState.Idle, awaitItem())
            viewModel.verifyOtp("user@example.com", "123456")
            assertEquals(VerifyOtpUiState.Loading, awaitItem())
            assertEquals(VerifyOtpUiState.Success("123456"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _03_ShouldExposeVerifiedOtpValue_InSuccessState() = runTest(testDispatcher) {
        val useCase = mockk<VerifyPasswordResetOtpUseCase>()
        val viewModel = VerifyOtpViewModel(useCase)

        coEvery { useCase(any(), any()) } returns Result.success(Unit)

        viewModel.uiState.test {
            assertEquals(VerifyOtpUiState.Idle, awaitItem())
            viewModel.verifyOtp("user@example.com", "987654")
            assertEquals(VerifyOtpUiState.Loading, awaitItem())
            val success = awaitItem()
            assertTrue(success is VerifyOtpUiState.Success)
            assertEquals("987654", (success as VerifyOtpUiState.Success).otp)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _04_ShouldEmitLoadingThenError_WhenOtpIsInvalid() = runTest(testDispatcher) {
        val useCase = mockk<VerifyPasswordResetOtpUseCase>()
        val viewModel = VerifyOtpViewModel(useCase)

        coEvery { useCase(any(), any()) } returns
            Result.failure(RuntimeException("Invalid or expired OTP"))

        viewModel.uiState.test {
            assertEquals(VerifyOtpUiState.Idle, awaitItem())
            viewModel.verifyOtp("user@example.com", "000000")
            assertEquals(VerifyOtpUiState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is VerifyOtpUiState.Error)
            assertEquals("Invalid or expired OTP", (error as VerifyOtpUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _05_ShouldUseFallbackMessage_WhenExceptionHasNoMessage() = runTest(testDispatcher) {
        val useCase = mockk<VerifyPasswordResetOtpUseCase>()
        val viewModel = VerifyOtpViewModel(useCase)

        coEvery { useCase(any(), any()) } returns Result.failure(RuntimeException())

        viewModel.uiState.test {
            assertEquals(VerifyOtpUiState.Idle, awaitItem())
            viewModel.verifyOtp("user@example.com", "123456")
            assertEquals(VerifyOtpUiState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is VerifyOtpUiState.Error)
            assertEquals("Invalid OTP", (error as VerifyOtpUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _06_ShouldReturnStateToIdle_WhenResetStateIsCalled() = runTest(testDispatcher) {
        val useCase = mockk<VerifyPasswordResetOtpUseCase>()
        val viewModel = VerifyOtpViewModel(useCase)

        coEvery { useCase(any(), any()) } returns
            Result.failure(RuntimeException("Network error"))

        viewModel.uiState.test {
            assertEquals(VerifyOtpUiState.Idle, awaitItem())
            viewModel.verifyOtp("user@example.com", "123456")
            assertEquals(VerifyOtpUiState.Loading, awaitItem())
            assertTrue(awaitItem() is VerifyOtpUiState.Error)
            viewModel.resetState()
            assertEquals(VerifyOtpUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
