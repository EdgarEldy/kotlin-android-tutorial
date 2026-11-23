package com.edgareldy.tutorial.features.product.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.edgareldy.tutorial.domain.model.Product
import com.edgareldy.tutorial.domain.usecase.product.GetProductByIdUseCase
import com.edgareldy.tutorial.navigation.Screen
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
 * Unit tests for ProductDetailViewModel.
 * Verifies the state machine: Loading is the initial state, then Success or
 * Error depending on the use case result. Also covers retry(), which resets
 * to Loading and triggers a fresh collection.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProductDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var useCase: GetProductByIdUseCase
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        useCase = mockk()
        savedStateHandle = SavedStateHandle(
            mapOf(Screen.ProductDetail.ARG_PRODUCT_ID to 42L)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Helpers ---

    private fun fakeProduct() = Product(
        id = 42L,
        name = "iPhone 15",
        unitPrice = 999.0,
        description = "Latest iPhone",
        active = true,
        categoryId = 1L,
        categoryName = "Phones",
        images = emptyList()
    )

    private fun createViewModel() = ProductDetailViewModel(savedStateHandle, useCase)

    // --- Tests ---

    @Test
    fun _01_ShouldHaveLoadingAsInitialState_WhenViewModelIsCreated() = runTest(testDispatcher) {
        every { useCase(42L) } returns flowOf(Result.success(fakeProduct()))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(ProductDetailUiState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _02_ShouldEmitSuccess_WhenUseCaseReturnsProduct() = runTest(testDispatcher) {
        every { useCase(42L) } returns flowOf(Result.success(fakeProduct()))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(ProductDetailUiState.Loading, awaitItem())
            val state = awaitItem()
            assertTrue(state is ProductDetailUiState.Success)
            assertEquals("iPhone 15", (state as ProductDetailUiState.Success).product.name)
            assertEquals(999.0, state.product.unitPrice, 0.01)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _03_ShouldEmitError_WhenUseCaseReturnsFailure() = runTest(testDispatcher) {
        every { useCase(42L) } returns
            flowOf(Result.failure(RuntimeException("Product not found")))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(ProductDetailUiState.Loading, awaitItem())
            val state = awaitItem()
            assertTrue(state is ProductDetailUiState.Error)
            assertEquals("Product not found", (state as ProductDetailUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _04_ShouldUseDefaultErrorMessage_WhenExceptionMessageIsNull() = runTest(testDispatcher) {
        every { useCase(42L) } returns
            flowOf(Result.failure(RuntimeException()))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading
            val state = awaitItem()
            assertTrue(state is ProductDetailUiState.Error)
            assertEquals(
                "Failed to load product",
                (state as ProductDetailUiState.Error).message
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _05_ShouldReloadProduct_WhenRetryIsCalled() = runTest(testDispatcher) {
        every { useCase(42L) } returns flowOf(Result.success(fakeProduct()))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Loading (initial)
            awaitItem() // Success (initial load)

            viewModel.retry()

            assertEquals(ProductDetailUiState.Loading, awaitItem())
            assertTrue(awaitItem() is ProductDetailUiState.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
