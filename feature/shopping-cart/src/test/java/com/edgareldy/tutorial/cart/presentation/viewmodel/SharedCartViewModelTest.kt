package com.edgareldy.tutorial.cart.presentation.viewmodel

import com.edgareldy.tutorial.cart.domain.usecases.GetCartItemCountUseCase
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

/**
 * Unit tests for SharedCartViewModel.
 * Verifies that the cart badge count initializes from the use case on creation
 * and that refreshCount() updates it, including the post-checkout case where
 * the backend has emptied the cart and the count must drop to 0.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SharedCartViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var getCartItemCountUseCase: GetCartItemCountUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getCartItemCountUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SharedCartViewModel(getCartItemCountUseCase)

    @Test
    fun _01_ShouldInitializeCountFromUseCase_WhenViewModelIsCreated() = runTest(testDispatcher) {
        coEvery { getCartItemCountUseCase() } returns 3

        val viewModel = createViewModel()

        assertEquals(3, viewModel.cartItemCount.value)
    }

    @Test
    fun _02_ShouldUpdateCount_WhenRefreshCountIsCalledWithNonEmptyCart() =
        runTest(testDispatcher) {
            coEvery { getCartItemCountUseCase() } returns 0
            val viewModel = createViewModel()

            coEvery { getCartItemCountUseCase() } returns 5
            viewModel.refreshCount()

            assertEquals(5, viewModel.cartItemCount.value)
        }

    @Test
    fun _03_ShouldResetCountToZero_WhenRefreshCountIsCalledAfterCheckout() =
        runTest(testDispatcher) {
            // Simulates the post-checkout scenario: the backend cleared the cart,
            // so the use case now returns 0. The badge must disappear.
            coEvery { getCartItemCountUseCase() } returns 2
            val viewModel = createViewModel()
            assertEquals(2, viewModel.cartItemCount.value)

            coEvery { getCartItemCountUseCase() } returns 0
            viewModel.refreshCount()

            assertEquals(0, viewModel.cartItemCount.value)
        }

    @Test
    fun _04_ShouldKeepCountAtZero_WhenUseCaseReturnsZeroOnInit() = runTest(testDispatcher) {
        coEvery { getCartItemCountUseCase() } returns 0

        val viewModel = createViewModel()

        assertEquals(0, viewModel.cartItemCount.value)
    }
}
