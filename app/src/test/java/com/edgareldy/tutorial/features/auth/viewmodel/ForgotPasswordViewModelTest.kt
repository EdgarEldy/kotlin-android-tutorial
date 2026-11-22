package com.edgareldy.tutorial.features.auth.viewmodel

import com.edgareldy.tutorial.domain.usecase.auth.SendPasswordResetOtpUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

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
    fun _01_ShouldReturnRequestAccepted_WhenUseCaseSucceeds() = runTest(testDispatcher) {
        val useCase = mockk<SendPasswordResetOtpUseCase>()
        coEvery { useCase(any()) } returns Result.success(Unit)
        val viewModel = ForgotPasswordViewModel(useCase)

        viewModel.sendOtp("user@example.com")

        assertEquals(
            ForgotPasswordUiState.RequestAccepted("user@example.com"),
            viewModel.uiState.value
        )
    }

    @Test
    fun _02_ShouldReturnError_WhenUseCaseFails() = runTest(testDispatcher) {
        val useCase = mockk<SendPasswordResetOtpUseCase>()
        coEvery { useCase(any()) } returns Result.failure(RuntimeException("Network error"))
        val viewModel = ForgotPasswordViewModel(useCase)

        viewModel.sendOtp("user@example.com")

        assertEquals(ForgotPasswordUiState.Error("Network error"), viewModel.uiState.value)
    }
}
