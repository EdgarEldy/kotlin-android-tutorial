package com.edgareldy.tutorial.features.auth.viewmodel

import com.edgareldy.tutorial.domain.usecase.auth.ConfirmPasswordResetUseCase
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
 * Unit tests for ResetPasswordViewModel.
 * Verifies UiState transitions (Idle, Loading, Success, Error) driven by
 * ConfirmPasswordResetUseCase results (step 3 of the mobile password reset flow),
 * and that the resetState action restores Idle.
 * Uses UnconfinedTestDispatcher so viewModelScope.launch runs eagerly.
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

        assertEquals(ResetPasswordUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun _02_ShouldSetStateToLoading_WhileUseCaseExecutes() = runTest(testDispatcher) {
        val useCase = mockk<ConfirmPasswordResetUseCase>()
        val viewModel = ResetPasswordViewModel(useCase)

        // delay forces a real suspension point so Loading is observable before Success
        coEvery { useCase(any(), any(), any()) } coAnswers {
            delay(1_000)
            Result.success(Unit)
        }

        viewModel.resetPassword("123456", "newPassword1", "newPassword1")

        assertEquals(ResetPasswordUiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()
        assertEquals(ResetPasswordUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun _03_ShouldUpdateStateToSuccess_WhenPasswordResetSucceeds() = runTest(testDispatcher) {
        val useCase = mockk<ConfirmPasswordResetUseCase>()
        val viewModel = ResetPasswordViewModel(useCase)
        coEvery { useCase(any(), any(), any()) } returns Result.success(Unit)

        viewModel.resetPassword("123456", "newPassword1", "newPassword1")
        advanceUntilIdle()

        assertEquals(ResetPasswordUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun _04_ShouldUpdateStateToError_WhenPasswordResetFails() = runTest(testDispatcher) {
        val useCase = mockk<ConfirmPasswordResetUseCase>()
        val viewModel = ResetPasswordViewModel(useCase)
        coEvery { useCase(any(), any(), any()) } returns
            Result.failure(RuntimeException("Token expired"))

        viewModel.resetPassword("123456", "newPassword1", "newPassword1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ResetPasswordUiState.Error)
        assertEquals("Token expired", (state as ResetPasswordUiState.Error).message)
    }

    @Test
    fun _05_ShouldUpdateStateToError_WhenUseCaseValidationFails() = runTest(testDispatcher) {
        val useCase = mockk<ConfirmPasswordResetUseCase>()
        val viewModel = ResetPasswordViewModel(useCase)
        coEvery { useCase(any(), any(), any()) } returns
            Result.failure(IllegalArgumentException("Passwords do not match"))

        viewModel.resetPassword("123456", "newPassword1", "differentPassword")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ResetPasswordUiState.Error)
        assertEquals("Passwords do not match", (state as ResetPasswordUiState.Error).message)
    }

    @Test
    fun _06_ShouldUseFallbackMessage_WhenExceptionHasNoMessage() = runTest(testDispatcher) {
        val useCase = mockk<ConfirmPasswordResetUseCase>()
        val viewModel = ResetPasswordViewModel(useCase)
        coEvery { useCase(any(), any(), any()) } returns Result.failure(RuntimeException())

        viewModel.resetPassword("123456", "newPassword1", "newPassword1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ResetPasswordUiState.Error)
        assertEquals("Password reset failed", (state as ResetPasswordUiState.Error).message)
    }

    @Test
    fun _07_ShouldReturnStateToIdle_WhenResetStateIsCalled() = runTest(testDispatcher) {
        val useCase = mockk<ConfirmPasswordResetUseCase>()
        val viewModel = ResetPasswordViewModel(useCase)
        coEvery { useCase(any(), any(), any()) } returns
            Result.failure(RuntimeException("Network error"))
        viewModel.resetPassword("123456", "newPassword1", "newPassword1")
        advanceUntilIdle()

        viewModel.resetState()

        assertEquals(ResetPasswordUiState.Idle, viewModel.uiState.value)
    }
}
