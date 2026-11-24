package com.edgareldy.tutorial.orders.presentation.viewmodel

import app.cash.turbine.test
import com.edgareldy.tutorial.orders.domain.model.Order
import com.edgareldy.tutorial.orders.domain.model.OrderStatus
import com.edgareldy.tutorial.orders.domain.usecases.CreateOrderUseCase
import io.mockk.coAnswers
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
 * Verifies the initial Idle state and the two execution paths of confirmOrder():
 * Idle -> Loading -> Success and Idle -> Loading -> Error.
 * delay(1) in coAnswers ensures the Loading state is observable before resolution.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutConfirmationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var createOrderUseCase: CreateOrderUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        createOrderUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fakeOrder(id: Long) = Order(
        id = id,
        orderNumber = "ORD-$id",
        status = OrderStatus.PENDING,
        totalAmount = 320.0,
        items = emptyList()
    )

    @Test
    fun _01_ShouldHaveIdleAsInitialState_WhenViewModelIsCreated() = runTest(testDispatcher) {
        val viewModel = CheckoutConfirmationViewModel(createOrderUseCase)

        assertEquals(CheckoutUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun _02_ShouldEmitLoadingThenSuccess_WhenConfirmOrderSucceeds() = runTest(testDispatcher) {
        val order = fakeOrder(5L)
        coEvery { createOrderUseCase() } coAnswers { delay(1); Result.success(order) }

        val viewModel = CheckoutConfirmationViewModel(createOrderUseCase)

        viewModel.uiState.test {
            assertEquals(CheckoutUiState.Idle, awaitItem())
            viewModel.confirmOrder()
            assertEquals(CheckoutUiState.Loading, awaitItem())
            assertEquals(CheckoutUiState.Success(order.id), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _03_ShouldEmitLoadingThenError_WhenConfirmOrderFails() = runTest(testDispatcher) {
        coEvery { createOrderUseCase() } coAnswers {
            delay(1)
            Result.failure(RuntimeException("Server error"))
        }

        val viewModel = CheckoutConfirmationViewModel(createOrderUseCase)

        viewModel.uiState.test {
            assertEquals(CheckoutUiState.Idle, awaitItem())
            viewModel.confirmOrder()
            assertEquals(CheckoutUiState.Loading, awaitItem())
            val state = awaitItem()
            assertTrue(state is CheckoutUiState.Error)
            assertEquals("Server error", (state as CheckoutUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
