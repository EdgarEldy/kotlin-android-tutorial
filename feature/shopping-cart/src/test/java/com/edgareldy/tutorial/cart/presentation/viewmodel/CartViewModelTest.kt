package com.edgareldy.tutorial.cart.presentation.viewmodel

import app.cash.turbine.test
import com.edgareldy.tutorial.cart.domain.model.Cart
import com.edgareldy.tutorial.cart.domain.model.CartItem
import com.edgareldy.tutorial.cart.domain.usecases.ClearCartUseCase
import com.edgareldy.tutorial.cart.domain.usecases.GetCartUseCase
import com.edgareldy.tutorial.cart.domain.usecases.RemoveCartItemUseCase
import com.edgareldy.tutorial.cart.domain.usecases.UpdateCartItemUseCase
import io.mockk.coEvery
import io.mockk.coVerify
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
 * Unit tests for CartViewModel.
 * Verifies the complete UiState transition sequence (Loading, Success, Empty, Error)
 * and all mutation actions (removeItem, decreaseQuantity, increaseQuantity, clearCart).
 * Turbine is used to assert each StateFlow emission in order.
 *
 * With UnconfinedTestDispatcher, a mock that returns immediately causes the init
 * coroutine to complete before Turbine subscribes. To capture Loading, the use-case
 * mock must include a delay(1) so the coroutine suspends at that point and leaves
 * the state at Loading when the collector attaches.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var getCartUseCase: GetCartUseCase
    private lateinit var updateCartItemUseCase: UpdateCartItemUseCase
    private lateinit var removeCartItemUseCase: RemoveCartItemUseCase
    private lateinit var clearCartUseCase: ClearCartUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getCartUseCase = mockk()
        updateCartItemUseCase = mockk()
        removeCartItemUseCase = mockk()
        clearCartUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fakeItem(id: Long, quantity: Int = 1) = CartItem(
        id = id,
        productId = 10L + id,
        productName = "Product $id",
        unitPrice = 9.99,
        quantity = quantity,
        subtotal = 9.99 * quantity
    )

    private fun fakeCart(vararg items: CartItem) = Cart(
        id = 1L,
        items = items.toList(),
        total = items.sumOf { it.subtotal }
    )

    private fun createViewModel() = CartViewModel(
        getCartUseCase,
        updateCartItemUseCase,
        removeCartItemUseCase,
        clearCartUseCase
    )

    @Test
    fun _01_ShouldEmitLoadingThenSuccess_WhenGetCartReturnsItems() = runTest(testDispatcher) {
        // delay(1) makes the loadCart coroutine suspend at the use-case call, leaving
        // Loading visible to Turbine before the coroutine resolves to Success.
        val cart = fakeCart(fakeItem(1L, 2), fakeItem(2L, 1))
        coEvery { getCartUseCase() } coAnswers {
            delay(1)
            Result.success(cart)
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(CartUiState.Loading, awaitItem())
            viewModel.loadCart()
            val state = awaitItem()
            assertTrue(state is CartUiState.Success)
            assertEquals(2, (state as CartUiState.Success).cart.items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _02_ShouldEmitLoadingThenEmpty_WhenGetCartReturnsNoItems() = runTest(testDispatcher) {
        val emptyCart = fakeCart()
        coEvery { getCartUseCase() } coAnswers {
            delay(1)
            Result.success(emptyCart)
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(CartUiState.Loading, awaitItem())
            viewModel.loadCart()
            assertEquals(CartUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _03_ShouldEmitLoadingThenError_WhenGetCartFails() = runTest(testDispatcher) {
        coEvery { getCartUseCase() } coAnswers {
            delay(1)
            Result.failure(RuntimeException("Network error"))
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(CartUiState.Loading, awaitItem())
            viewModel.loadCart()
            val state = awaitItem()
            assertTrue(state is CartUiState.Error)
            assertEquals("Network error", (state as CartUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _04_ShouldUseDefaultErrorMessage_WhenExceptionMessageIsNull() = runTest(testDispatcher) {
        coEvery { getCartUseCase() } returns Result.failure(RuntimeException())

        val viewModel = createViewModel()
        viewModel.loadCart()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is CartUiState.Error)
            assertEquals("Failed to load cart", (state as CartUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _05_ShouldUpdateStateAfterRemoveItem_WhenRemoveSucceeds() = runTest(testDispatcher) {
        val cartBefore = fakeCart(fakeItem(1L, 1), fakeItem(2L, 2))
        val cartAfter = fakeCart(fakeItem(2L, 2))
        coEvery { getCartUseCase() } returns Result.success(cartBefore)
        coEvery { removeCartItemUseCase(1L) } returns Result.success(cartAfter)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Success from init

            viewModel.removeItem(1L)

            val state = awaitItem()
            assertTrue(state is CartUiState.Success)
            assertEquals(1, (state as CartUiState.Success).cart.items.size)
            assertEquals(2L, state.cart.items[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _06_ShouldEmitEmpty_WhenRemoveLastItemSucceeds() = runTest(testDispatcher) {
        val cartBefore = fakeCart(fakeItem(1L, 1))
        val emptyCart = fakeCart()
        coEvery { getCartUseCase() } returns Result.success(cartBefore)
        coEvery { removeCartItemUseCase(1L) } returns Result.success(emptyCart)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Success from init

            viewModel.removeItem(1L)

            assertEquals(CartUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _07_ShouldCallRemoveItem_WhenDecreaseQuantityCalledWithQuantityOne() =
        runTest(testDispatcher) {
            val cartBefore = fakeCart(fakeItem(1L, 1))
            val emptyCart = fakeCart()
            coEvery { getCartUseCase() } returns Result.success(cartBefore)
            coEvery { removeCartItemUseCase(1L) } returns Result.success(emptyCart)

            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() // Success from init

                viewModel.decreaseQuantity(itemId = 1L, currentQuantity = 1)

                awaitItem() // Empty

                coVerify(exactly = 1) { removeCartItemUseCase(1L) }
                coVerify(exactly = 0) { updateCartItemUseCase(any(), any()) }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun _08_ShouldCallUpdateItemWithDecrementedQuantity_WhenDecreaseQuantityCalledWithQuantityTwo() =
        runTest(testDispatcher) {
            val cartBefore = fakeCart(fakeItem(1L, 2))
            val cartAfter = fakeCart(fakeItem(1L, 1))
            coEvery { getCartUseCase() } returns Result.success(cartBefore)
            coEvery { updateCartItemUseCase(1L, 1) } returns Result.success(cartAfter)

            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() // Success with quantity=2

                viewModel.decreaseQuantity(itemId = 1L, currentQuantity = 2)

                val state = awaitItem()
                assertTrue(state is CartUiState.Success)
                assertEquals(1, (state as CartUiState.Success).cart.items[0].quantity)

                coVerify(exactly = 1) { updateCartItemUseCase(1L, 1) }
                coVerify(exactly = 0) { removeCartItemUseCase(any()) }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun _09_ShouldEmitEmptyAfterClearCart_WhenClearCartSucceeds() = runTest(testDispatcher) {
        val cart = fakeCart(fakeItem(1L, 2))
        val emptyCart = fakeCart()
        coEvery { getCartUseCase() } returns Result.success(cart)
        coEvery { clearCartUseCase() } returns Result.success(emptyCart)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Success from init

            viewModel.clearCart()

            assertEquals(CartUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _10_ShouldEmitLoadingThenSuccess_WhenLoadCartIsCalledExplicitly() =
        runTest(testDispatcher) {
            val cart = fakeCart(fakeItem(1L, 1))
            coEvery { getCartUseCase() } returns Result.success(cart)

            val viewModel = createViewModel()
            viewModel.loadCart()

            viewModel.uiState.test {
                awaitItem() // Success from initial loadCart()

                coEvery { getCartUseCase() } coAnswers {
                    delay(1)
                    Result.success(cart)
                }
                viewModel.loadCart()

                assertEquals(CartUiState.Loading, awaitItem())
                assertTrue(awaitItem() is CartUiState.Success)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
