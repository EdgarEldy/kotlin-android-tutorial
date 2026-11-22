package com.edgareldy.tutorial.features.auth.viewmodel

import app.cash.turbine.test
import com.edgareldy.tutorial.domain.usecase.auth.ConfirmPasswordResetUseCase
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
 * Unit tests for ResetPasswordViewModel.
 * Verifies the complete UiState emission sequence (Idle -> Loading -> Success/Error)
 * driven by ConfirmPasswordResetUseCase results (step 3 of the mobile password reset flow),
 * using Turbine to assert each state transition. Covers token expiry, password mismatch,
 * fallback message and resetState behaviour.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ResetPasswordViewModelTest {

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
    fun _01_ShouldBeIdle_WhenNoResetAttemptHasBeenMade() = runTest(testDispatcher) {
        val useCase = mockk<ConfirmPasswordResetUseCase>()
        val viewModel = ResetPasswordViewModel(useCase)

        viewModel.uiState.test {
            assertEquals(ResetPasswordUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _02_ShouldEmitLoadingThenSuccess_WhenPasswordResetSucceeds() = runTest(testDispatcher) {
        val useCase = mockk<ConfirmPasswordResetUseCase>()
        val viewModel = ResetPasswordViewModel(useCase)

        coEvery { useCase(any(), any(), any()) } coAnswers {
            delay(100)
            Result.success(Unit)
        }

        viewModel.uiState.test {
            assertEquals(ResetPasswordUiState.Idle, awaitItem())
            viewModel.resetPassword("123456", "newPassword1", "newPassword1")
            assertEquals(ResetPasswordUiState.Loading, awaitItem())
            assertEquals(ResetPasswordUiState.Success, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _03_ShouldEmitLoadingThenError_WhenPasswordResetFails() = runTest(testDispatcher) {
        val useCase = mockk<ConfirmPasswordResetUseCase>()
        val viewModel = ResetPasswordViewModel(useCase)

        coEvery { useCase(any(), any(), any()) } returns
            Result.failure(RuntimeException("Token expired"))

        viewModel.uiState.test {
            assertEquals(ResetPasswordUiState.Idle, awaitItem())
            viewModel.resetPassword("123456", "newPassword1", "newPassword1")
            assertEquals(ResetPasswordUiState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is ResetPasswordUiState.Error)
            assertEquals("Token expired", (error as ResetPasswordUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _04_ShouldEmitError_WhenPasswordsDoNotMatch() = runTest(testDispatcher) {
        val useCase = mockk<ConfirmPasswordResetUseCase>()
        val viewModel = ResetPasswordViewModel(useCase)

        coEvery { useCase(any(), any(), any()) } returns
            Result.failure(IllegalArgumentException("Passwords do not match"))

        viewModel.uiState.test {
            assertEquals(ResetPasswordUiState.Idle, awaitItem())
            viewModel.resetPassword("123456", "newPassword1", "differentPassword")
            assertEquals(ResetPasswordUiState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is ResetPasswordUiState.Error)
            assertEquals("Passwords do not match", (error as ResetPasswordUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _05_ShouldUseFallbackMessage_WhenExceptionHasNoMessage() = runTest(testDispatcher) {
        val useCase = mockk<ConfirmPasswordResetUseCase>()
        val viewModel = ResetPasswordViewModel(useCase)

        coEvery { useCase(any(), any(), any()) } returns Result.failure(RuntimeException())

        viewModel.uiState.test {
            assertEquals(ResetPasswordUiState.Idle, awaitItem())
            viewModel.resetPassword("123456", "newPassword1", "newPassword1")
            assertEquals(ResetPasswordUiState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is ResetPasswordUiState.Error)
            assertEquals("Password reset failed", (error as ResetPasswordUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _06_ShouldReturnStateToIdle_WhenResetStateIsCalled() = runTest(testDispatcher) {
        val useCase = mockk<ConfirmPasswordResetUseCase>()
        val viewModel = ResetPasswordViewModel(useCase)

        coEvery { useCase(any(), any(), any()) } returns
            Result.failure(RuntimeException("Network error"))

        viewModel.uiState.test {
            assertEquals(ResetPasswordUiState.Idle, awaitItem())
            viewModel.resetPassword("123456", "newPassword1", "newPassword1")
            assertEquals(ResetPasswordUiState.Loading, awaitItem())
            assertTrue(awaitItem() is ResetPasswordUiState.Error)
            viewModel.resetState()
            assertEquals(ResetPasswordUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
