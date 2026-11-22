package com.edgareldy.tutorial.features.auth.viewmodel

import com.edgareldy.tutorial.domain.usecase.auth.VerifyPasswordResetOtpUseCase
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
 * Unit tests for VerifyOtpViewModel.
 * Verifies UiState transitions (Idle, Loading, Success, Error) driven by
 * VerifyPasswordResetOtpUseCase results (step 2 of the mobile password reset flow),
 * that Success carries the verified OTP value for forwarding to the next screen,
 * and that the resetState action restores Idle.
 * Uses UnconfinedTestDispatcher so viewModelScope.launch runs eagerly.
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

        assertEquals(VerifyOtpUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun _02_ShouldSetStateToLoading_WhileUseCaseExecutes() = runTest(testDispatcher) {
        val useCase = mockk<VerifyPasswordResetOtpUseCase>()
        val viewModel = VerifyOtpViewModel(useCase)

        // delay forces a real suspension point so Loading is observable before Success
        coEvery { useCase(any(), any()) } coAnswers {
            delay(1_000)
            Result.success(Unit)
        }

        viewModel.verifyOtp("user@example.com", "123456")

        assertEquals(VerifyOtpUiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()
        assertEquals(VerifyOtpUiState.Success("123456"), viewModel.uiState.value)
    }

    @Test
    fun _03_ShouldUpdateStateToSuccess_WhenOtpIsVerified() = runTest(testDispatcher) {
        val useCase = mockk<VerifyPasswordResetOtpUseCase>()
        val viewModel = VerifyOtpViewModel(useCase)
        coEvery { useCase(any(), any()) } returns Result.success(Unit)

        viewModel.verifyOtp("user@example.com", "123456")
        advanceUntilIdle()

        assertEquals(VerifyOtpUiState.Success("123456"), viewModel.uiState.value)
    }

    @Test
    fun _04_ShouldExposeVerifiedOtpValue_InSuccessState() = runTest(testDispatcher) {
        val useCase = mockk<VerifyPasswordResetOtpUseCase>()
        val viewModel = VerifyOtpViewModel(useCase)
        coEvery { useCase(any(), any()) } returns Result.success(Unit)

        viewModel.verifyOtp("user@example.com", "987654")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is VerifyOtpUiState.Success)
        assertEquals("987654", (state as VerifyOtpUiState.Success).otp)
    }

    @Test
    fun _05_ShouldUpdateStateToError_WhenOtpIsInvalid() = runTest(testDispatcher) {
        val useCase = mockk<VerifyPasswordResetOtpUseCase>()
        val viewModel = VerifyOtpViewModel(useCase)
        coEvery { useCase(any(), any()) } returns
            Result.failure(RuntimeException("Invalid or expired OTP"))

        viewModel.verifyOtp("user@example.com", "000000")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is VerifyOtpUiState.Error)
        assertEquals("Invalid or expired OTP", (state as VerifyOtpUiState.Error).message)
    }

    @Test
    fun _06_ShouldUseFallbackMessage_WhenExceptionHasNoMessage() = runTest(testDispatcher) {
        val useCase = mockk<VerifyPasswordResetOtpUseCase>()
        val viewModel = VerifyOtpViewModel(useCase)
        coEvery { useCase(any(), any()) } returns Result.failure(RuntimeException())

        viewModel.verifyOtp("user@example.com", "123456")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is VerifyOtpUiState.Error)
        assertEquals("Invalid OTP", (state as VerifyOtpUiState.Error).message)
    }

    @Test
    fun _07_ShouldReturnStateToIdle_WhenResetStateIsCalled() = runTest(testDispatcher) {
        val useCase = mockk<VerifyPasswordResetOtpUseCase>()
        val viewModel = VerifyOtpViewModel(useCase)
        coEvery { useCase(any(), any()) } returns
            Result.failure(RuntimeException("Network error"))
        viewModel.verifyOtp("user@example.com", "123456")
        advanceUntilIdle()

        viewModel.resetState()

        assertEquals(VerifyOtpUiState.Idle, viewModel.uiState.value)
    }
}
