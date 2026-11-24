package com.edgareldy.tutorial.orders.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.edgareldy.tutorial.orders.domain.model.Order
import com.edgareldy.tutorial.orders.domain.model.OrderItem
import com.edgareldy.tutorial.orders.domain.model.OrderStatus
import com.edgareldy.tutorial.orders.domain.usecases.GetOrderByIdUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
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
 * Unit tests for OrderDetailViewModel.
 * Verifies the UiState transitions when fetching a single order by id,
 * covering the success path and the not-found error path.
 * SavedStateHandle is instantiated directly with the orderId key.
 * flow { delay(1); emit(...) } is used so that Loading is observable
 * before the state transitions, since there is no debounce in the ViewModel.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OrderDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var getOrderByIdUseCase: GetOrderByIdUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getOrderByIdUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(orderId: Long): OrderDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("orderId" to orderId))
        return OrderDetailViewModel(savedStateHandle, getOrderByIdUseCase)
    }

    private fun fakeOrder(id: Long) = Order(
        id = id,
        orderNumber = "ORD-$id",
        status = OrderStatus.CONFIRMED,
        totalAmount = 199.99,
        items = listOf(
            OrderItem(
                id = 1L,
                productId = 10L,
                productName = "Product A",
                unitPrice = 99.99,
                quantity = 2,
                subtotal = 199.99
            )
        )
    )

    @Test
    fun _01_ShouldEmitLoadingThenSuccess_WhenUseCaseEmitsOrder() = runTest(testDispatcher) {
        val order = fakeOrder(42L)
        every { getOrderByIdUseCase(42L) } returns flow { delay(1); emit(Result.success(order)) }

        val viewModel = createViewModel(42L)

        viewModel.uiState.test {
            assertEquals(OrderDetailUiState.Loading, awaitItem())
            assertEquals(OrderDetailUiState.Success(order), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _02_ShouldEmitError_WhenOrderIsNotFound() = runTest(testDispatcher) {
        every { getOrderByIdUseCase(99L) } returns
            flow { delay(1); emit(Result.failure<Order>(RuntimeException("Order not found"))) }

        val viewModel = createViewModel(99L)

        viewModel.uiState.test {
            assertEquals(OrderDetailUiState.Loading, awaitItem())
            val state = awaitItem()
            assertTrue(state is OrderDetailUiState.Error)
            assertEquals("Order not found", (state as OrderDetailUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
