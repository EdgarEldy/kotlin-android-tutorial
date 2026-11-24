package com.edgareldy.tutorial.auth.presentation.viewmodel

import app.cash.turbine.test
import com.edgareldy.tutorial.auth.domain.usecases.ActivateAccountUseCase
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
 * Unit tests for ActivateAccountViewModel.
 * Verifies the complete UiState emission sequence (Idle -> Loading -> Success/Error)
 * driven by ActivateAccountUseCase results, using Turbine to assert each state transition.
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

        viewModel.uiState.test {
            assertEquals(ActivateAccountUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _02_ShouldEmitLoadingThenSuccess_WhenActivationSucceeds() = runTest(testDispatcher) {
        val useCase = mockk<ActivateAccountUseCase>()
        val viewModel = ActivateAccountViewModel(useCase)

        coEvery { useCase(any(), any()) } coAnswers {
            delay(100)
            Result.success(Unit)
        }

        viewModel.uiState.test {
            assertEquals(ActivateAccountUiState.Idle, awaitItem())
            viewModel.activate("user@example.com", "123456")
            assertEquals(ActivateAccountUiState.Loading, awaitItem())
            assertEquals(ActivateAccountUiState.Success, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _03_ShouldEmitLoadingThenError_WhenActivationFails() = runTest(testDispatcher) {
        val useCase = mockk<ActivateAccountUseCase>()
        val viewModel = ActivateAccountViewModel(useCase)

        coEvery { useCase(any(), any()) } returns
            Result.failure(RuntimeException("Activation code expired"))

        viewModel.uiState.test {
            assertEquals(ActivateAccountUiState.Idle, awaitItem())
            viewModel.activate("user@example.com", "123456")
            assertEquals(ActivateAccountUiState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is ActivateAccountUiState.Error)
            assertEquals("Activation code expired", (error as ActivateAccountUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _04_ShouldUseFallbackMessage_WhenExceptionHasNoMessage() = runTest(testDispatcher) {
        val useCase = mockk<ActivateAccountUseCase>()
        val viewModel = ActivateAccountViewModel(useCase)

        coEvery { useCase(any(), any()) } returns Result.failure(RuntimeException())

        viewModel.uiState.test {
            assertEquals(ActivateAccountUiState.Idle, awaitItem())
            viewModel.activate("user@example.com", "123456")
            assertEquals(ActivateAccountUiState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is ActivateAccountUiState.Error)
            assertEquals("Activation failed", (error as ActivateAccountUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _05_ShouldReturnStateToIdle_WhenResetStateIsCalled() = runTest(testDispatcher) {
        val useCase = mockk<ActivateAccountUseCase>()
        val viewModel = ActivateAccountViewModel(useCase)

        coEvery { useCase(any(), any()) } returns
            Result.failure(RuntimeException("Network error"))

        viewModel.uiState.test {
            assertEquals(ActivateAccountUiState.Idle, awaitItem())
            viewModel.activate("user@example.com", "123456")
            assertEquals(ActivateAccountUiState.Loading, awaitItem())
            assertTrue(awaitItem() is ActivateAccountUiState.Error)
            viewModel.resetState()
            assertEquals(ActivateAccountUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
