package com.edgareldy.tutorial.features.orders.viewmodel

import app.cash.turbine.test
import com.edgareldy.tutorial.domain.model.Order
import com.edgareldy.tutorial.domain.model.OrderStatus
import com.edgareldy.tutorial.domain.usecase.order.CreateOrderUseCase
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
 * Unit tests for CheckoutConfirmationViewModel.
 * Verifies the complete UiState transition sequence from Idle through
 * Loading to Success or Error, and that errorShown() resets the state to Idle.
 *
 * The delay(1) pattern in coAnswers is required to leave the state at Loading
 * long enough for Turbine to observe it before the coroutine resolves.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutConfirmationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var createOrderUseCase: CreateOrderUseCase

    // --- Helpers ---

    private fun fakeOrder(id: Long = 1L) = Order(
        id = id,
        orderNumber = "ORD-$id",
        status = OrderStatus.CONFIRMED,
        totalAmount = 59.99,
        items = emptyList()
    )

    private fun createViewModel() = CheckoutConfirmationViewModel(createOrderUseCase)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        createOrderUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Tests ---

    @Test
    fun _01_ShouldHaveIdleAsInitialState_WhenViewModelIsCreated() = runTest(testDispatcher) {
        // No mock needed - confirmOrder is not called
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(CheckoutUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _02_ShouldEmitLoadingThenSuccess_WhenConfirmOrderSucceeds() = runTest(testDispatcher) {
        // delay(1) makes confirmOrder suspend at the use-case boundary,
        // leaving the state at Loading so Turbine can observe it.
        val order = fakeOrder(42L)
        coEvery { createOrderUseCase() } coAnswers {
            delay(1)
            Result.success(order)
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(CheckoutUiState.Idle, awaitItem())

            viewModel.confirmOrder()

            assertEquals(CheckoutUiState.Loading, awaitItem())
            val state = awaitItem()
            assertEquals(CheckoutUiState.Success(42L), state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _03_ShouldEmitLoadingThenError_WhenConfirmOrderFails() = runTest(testDispatcher) {
        coEvery { createOrderUseCase() } coAnswers {
            delay(1)
            Result.failure(RuntimeException("Checkout failed"))
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(CheckoutUiState.Idle, awaitItem())

            viewModel.confirmOrder()

            assertEquals(CheckoutUiState.Loading, awaitItem())
            val state = awaitItem()
            assertTrue(state is CheckoutUiState.Error)
            assertEquals("Checkout failed", (state as CheckoutUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _04_ShouldTransitionBackToIdle_WhenErrorShownIsCalled() = runTest(testDispatcher) {
        // delay(1) ensures Loading is emitted as a distinct value before Error,
        // preventing StateFlow conflation from skipping it.
        coEvery { createOrderUseCase() } coAnswers {
            delay(1)
            Result.failure(RuntimeException("Server error"))
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(CheckoutUiState.Idle, awaitItem())

            viewModel.confirmOrder()

            assertEquals(CheckoutUiState.Loading, awaitItem())
            val errorState = awaitItem()
            assertTrue(errorState is CheckoutUiState.Error)

            viewModel.errorShown()

            assertEquals(CheckoutUiState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _05_ShouldIgnoreSecondConfirmCall_WhenAlreadyLoading() = runTest(testDispatcher) {
        // The guard in confirmOrder prevents re-entry when already loading.
        val order = fakeOrder(1L)
        coEvery { createOrderUseCase() } coAnswers {
            delay(1)
            Result.success(order)
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Idle

            viewModel.confirmOrder()
            // Second call while Loading is in progress - must be a no-op
            viewModel.confirmOrder()

            assertEquals(CheckoutUiState.Loading, awaitItem())
            assertTrue(awaitItem() is CheckoutUiState.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
