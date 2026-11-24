package com.edgareldy.tutorial.orders.presentation.viewmodel

import app.cash.turbine.test
import com.edgareldy.tutorial.orders.domain.model.Order
import com.edgareldy.tutorial.orders.domain.model.OrderStatus
import com.edgareldy.tutorial.orders.domain.usecases.GetOrdersUseCase
import com.edgareldy.tutorial.orders.domain.usecases.RefreshOrdersUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for OrderListViewModel.
 * Verifies the complete UiState transition sequence driven by GetOrdersUseCase,
 * as well as the isRefreshing flag managed by RefreshOrdersUseCase.
 * Turbine is used to assert each state change in emission order.
 * Unlike ProductListViewModel which uses debounce() to introduce a natural delay,
 * OrderListViewModel collects the flow eagerly. Using flow { delay(1); emit(...) }
 * in the mocks ensures Loading is observable before the state transitions.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OrderListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var getOrdersUseCase: GetOrdersUseCase
    private lateinit var refreshOrdersUseCase: RefreshOrdersUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getOrdersUseCase = mockk()
        refreshOrdersUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = OrderListViewModel(getOrdersUseCase, refreshOrdersUseCase)

    private fun fakeOrder(id: Long) = Order(
        id = id,
        orderNumber = "ORD-$id",
        status = OrderStatus.PENDING,
        totalAmount = 59.99,
        items = emptyList()
    )

    @Test
    fun _01_ShouldEmitLoadingThenSuccess_WhenUseCaseEmitsNonEmptyList() =
        runTest(testDispatcher) {
            val orders = listOf(fakeOrder(1L), fakeOrder(2L))
            every { getOrdersUseCase() } returns flow { delay(1); emit(Result.success(orders)) }
            coEvery { refreshOrdersUseCase() } returns Result.success(Unit)

            val viewModel = createViewModel()

            viewModel.uiState.test {
                assertEquals(OrderListUiState.Loading, awaitItem())
                assertEquals(OrderListUiState.Success(orders), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun _02_ShouldEmitEmpty_WhenUseCaseEmitsEmptyList() = runTest(testDispatcher) {
        every { getOrdersUseCase() } returns
            flow { delay(1); emit(Result.success<List<Order>>(emptyList())) }
        coEvery { refreshOrdersUseCase() } returns Result.success(Unit)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(OrderListUiState.Loading, awaitItem())
            assertEquals(OrderListUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _03_ShouldEmitError_WhenUseCaseEmitsFailure() = runTest(testDispatcher) {
        every { getOrdersUseCase() } returns
            flow { delay(1); emit(Result.failure<List<Order>>(RuntimeException("No internet"))) }
        coEvery { refreshOrdersUseCase() } returns Result.success(Unit)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(OrderListUiState.Loading, awaitItem())
            val state = awaitItem()
            assertTrue(state is OrderListUiState.Error)
            assertEquals("No internet", (state as OrderListUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _04_ShouldSetIsRefreshingTrueThenFalse_WhenRefreshIsCalled() =
        runTest(testDispatcher) {
            every { getOrdersUseCase() } returns flowOf(Result.success(emptyList()))
            coEvery { refreshOrdersUseCase() } coAnswers { delay(1); Result.success(Unit) }

            val viewModel = createViewModel()

            viewModel.isRefreshing.test {
                assertFalse(awaitItem())
                viewModel.refresh()
                assertTrue(awaitItem())
                assertFalse(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
}
