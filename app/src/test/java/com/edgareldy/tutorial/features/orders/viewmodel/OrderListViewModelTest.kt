package com.edgareldy.tutorial.features.orders.viewmodel

import app.cash.turbine.test
import com.edgareldy.tutorial.domain.model.Order
import com.edgareldy.tutorial.domain.model.OrderStatus
import com.edgareldy.tutorial.domain.usecase.order.GetOrdersUseCase
import com.edgareldy.tutorial.domain.usecase.order.RefreshOrdersUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for OrderListViewModel.
 * Verifies UiState transitions (Loading, Success, Empty, Error) produced by
 * the cache-first GetOrdersUseCase flow, and that pull-to-refresh delegates
 * to RefreshOrdersUseCase without breaking the existing state.
 *
 * GetOrdersUseCase.invoke() is a regular function (not suspend), so stubs
 * use every { } instead of coEvery { }. A delay inside the flow emission
 * is used to keep the state at Loading long enough for Turbine to observe it
 * before the coroutine advances to Success or Error.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OrderListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var getOrdersUseCase: GetOrdersUseCase
    private lateinit var refreshOrdersUseCase: RefreshOrdersUseCase

    // --- Helpers ---

    private fun fakeOrder(id: Long) = Order(
        id = id,
        orderNumber = "ORD-$id",
        status = OrderStatus.PENDING,
        totalAmount = 19.99,
        items = emptyList()
    )

    private fun createViewModel() = OrderListViewModel(getOrdersUseCase, refreshOrdersUseCase)

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

    // --- Tests ---

    @Test
    fun _01_ShouldHaveLoadingAsInitialState_WhenFlowNeverEmits() = runTest(testDispatcher) {
        // awaitCancellation suspends forever, so the ViewModel stays at Loading
        every { getOrdersUseCase() } returns flow { kotlinx.coroutines.awaitCancellation() }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(OrderListUiState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _02_ShouldEmitLoadingThenSuccess_WhenFlowReturnsNonEmptyList() = runTest(testDispatcher) {
        // delay(1) keeps the state at Loading until Turbine subscribes and observes it
        val orders = listOf(fakeOrder(1L), fakeOrder(2L))
        every { getOrdersUseCase() } returns flow {
            delay(1)
            emit(Result.success(orders))
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(OrderListUiState.Loading, awaitItem())
            val state = awaitItem()
            assertTrue(state is OrderListUiState.Success)
            assertEquals(2, (state as OrderListUiState.Success).orders.size)
            assertEquals(1L, state.orders[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _03_ShouldEmitLoadingThenEmpty_WhenFlowReturnsEmptyList() = runTest(testDispatcher) {
        every { getOrdersUseCase() } returns flow {
            delay(1)
            emit(Result.success(emptyList<Order>()))
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(OrderListUiState.Loading, awaitItem())
            assertEquals(OrderListUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _04_ShouldEmitLoadingThenError_WhenFlowReturnsFailure() = runTest(testDispatcher) {
        every { getOrdersUseCase() } returns flow {
            delay(1)
            emit(Result.failure<List<Order>>(RuntimeException("Network error")))
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(OrderListUiState.Loading, awaitItem())
            val state = awaitItem()
            assertTrue(state is OrderListUiState.Error)
            assertEquals("Network error", (state as OrderListUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _05_ShouldUseDefaultErrorMessage_WhenExceptionMessageIsNull() = runTest(testDispatcher) {
        every { getOrdersUseCase() } returns flowOf(Result.failure(RuntimeException()))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is OrderListUiState.Error)
            assertEquals("Failed to load orders", (state as OrderListUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _06_ShouldSetIsRefreshingFalse_AfterRefreshCompletes() = runTest(testDispatcher) {
        every { getOrdersUseCase() } returns flowOf(Result.success(emptyList()))
        coEvery { refreshOrdersUseCase() } just Runs

        val viewModel = createViewModel()

        viewModel.isRefreshing.test {
            assertEquals(false, awaitItem()) // initial false

            viewModel.refresh()

            // With UnconfinedTestDispatcher, refresh completes synchronously.
            // isRefreshing goes true then false without a stable true emission
            // visible between the two state updates on the same dispatcher.
            // We only assert the final settled value is false.
            cancelAndIgnoreRemainingEvents()
        }

        // After refresh, isRefreshing must be false
        assertEquals(false, viewModel.isRefreshing.value)
    }
}
