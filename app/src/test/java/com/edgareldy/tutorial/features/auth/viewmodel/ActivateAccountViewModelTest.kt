package com.edgareldy.tutorial.features.auth.viewmodel

import com.edgareldy.tutorial.domain.usecase.auth.ActivateAccountUseCase
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
 * Unit tests for ActivateAccountViewModel.
 * Verifies UiState transitions (Idle, Loading, Success, Error) driven by
 * ActivateAccountUseCase results, and the resetState action.
 * Uses UnconfinedTestDispatcher so viewModelScope.launch runs eagerly.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ActivateAccountViewModelTest {

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
    fun _01_ShouldBeIdle_WhenNoActivationAttemptHasBeenMade() = runTest(testDispatcher) {
        val useCase = mockk<ActivateAccountUseCase>()
        val viewModel = ActivateAccountViewModel(useCase)

        assertEquals(ActivateAccountUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun _02_ShouldSetStateToLoading_WhileUseCaseExecutes() = runTest(testDispatcher) {
        val useCase = mockk<ActivateAccountUseCase>()
        val viewModel = ActivateAccountViewModel(useCase)

        // delay forces a real suspension point so Loading is observable before Success
        coEvery { useCase(any(), any()) } coAnswers {
            delay(1_000)
            Result.success(Unit)
        }

        viewModel.activate("user@example.com", "123456")

        assertEquals(ActivateAccountUiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()
        assertEquals(ActivateAccountUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun _03_ShouldUpdateStateToSuccess_WhenActivationSucceeds() = runTest(testDispatcher) {
        val useCase = mockk<ActivateAccountUseCase>()
        val viewModel = ActivateAccountViewModel(useCase)
        coEvery { useCase(any(), any()) } returns Result.success(Unit)

        viewModel.activate("user@example.com", "123456")
        advanceUntilIdle()

        assertEquals(ActivateAccountUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun _04_ShouldUpdateStateToError_WhenActivationFails() = runTest(testDispatcher) {
        val useCase = mockk<ActivateAccountUseCase>()
        val viewModel = ActivateAccountViewModel(useCase)
        coEvery { useCase(any(), any()) } returns
            Result.failure(RuntimeException("Activation code expired"))

        viewModel.activate("user@example.com", "123456")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ActivateAccountUiState.Error)
        assertEquals("Activation code expired", (state as ActivateAccountUiState.Error).message)
    }

    @Test
    fun _05_ShouldUseFallbackMessage_WhenExceptionHasNoMessage() = runTest(testDispatcher) {
        val useCase = mockk<ActivateAccountUseCase>()
        val viewModel = ActivateAccountViewModel(useCase)
        coEvery { useCase(any(), any()) } returns Result.failure(RuntimeException())

        viewModel.activate("user@example.com", "123456")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ActivateAccountUiState.Error)
        assertEquals("Activation failed", (state as ActivateAccountUiState.Error).message)
    }

    @Test
    fun _06_ShouldReturnStateToIdle_WhenResetStateIsCalled() = runTest(testDispatcher) {
        val useCase = mockk<ActivateAccountUseCase>()
        val viewModel = ActivateAccountViewModel(useCase)
        coEvery { useCase(any(), any()) } returns
            Result.failure(RuntimeException("Network error"))
        viewModel.activate("user@example.com", "123456")
        advanceUntilIdle()

        viewModel.resetState()

        assertEquals(ActivateAccountUiState.Idle, viewModel.uiState.value)
    }
}
