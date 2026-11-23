package com.edgareldy.tutorial.features.products.viewmodel

import app.cash.turbine.test
import com.edgareldy.tutorial.domain.model.Category
import com.edgareldy.tutorial.domain.model.Product
import com.edgareldy.tutorial.domain.usecase.product.GetCategoriesUseCase
import com.edgareldy.tutorial.domain.usecase.product.GetProductsUseCase
import com.edgareldy.tutorial.domain.usecase.product.RefreshProductsUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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
 * Unit tests for ProductListViewModel.
 * Verifies the complete UiState emission sequence driven by GetProductsUseCase and
 * GetCategoriesUseCase, as well as side-effects of selectCategory, search, and refresh.
 * Turbine is used to assert each state transition in order on the StateFlow.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProductListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var getProductsUseCase: GetProductsUseCase
    private lateinit var getCategoriesUseCase: GetCategoriesUseCase
    private lateinit var refreshProductsUseCase: RefreshProductsUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getProductsUseCase = mockk()
        getCategoriesUseCase = mockk()
        refreshProductsUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Helpers ---

    private fun createViewModel(): ProductListViewModel =
        ProductListViewModel(getProductsUseCase, getCategoriesUseCase, refreshProductsUseCase)

    private fun fakeProduct(id: Long, name: String, categoryId: Long? = null) = Product(
        id = id,
        name = name,
        unitPrice = 9.99,
        description = null,
        active = true,
        categoryId = categoryId,
        categoryName = null,
        images = emptyList()
    )

    private fun fakeCategory(id: Long, name: String) = Category(id = id, name = name)

    // --- Tests ---

    @Test
    fun _01_ShouldHaveLoadingAsInitialState_WhenViewModelIsCreated() = runTest(testDispatcher) {
        every { getProductsUseCase(any(), any(), any(), any(), any(), any()) } returns
            flowOf(Result.success(emptyList()))
        every { getCategoriesUseCase() } returns
            flowOf(Result.success(emptyList()))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(ProductListUiState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _02_ShouldEmitSuccess_WhenBothUseCasesReturnData() = runTest(testDispatcher) {
        val products = listOf(fakeProduct(1L, "iPhone"), fakeProduct(2L, "MacBook"))
        val categories = listOf(fakeCategory(1L, "Electronics"))
        every { getProductsUseCase(any(), any(), any(), any(), any(), any()) } returns
            flowOf(Result.success(products))
        every { getCategoriesUseCase() } returns
            flowOf(Result.success(categories))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(ProductListUiState.Loading, awaitItem())
            val state = awaitItem()
            assertTrue(state is ProductListUiState.Success)
            with(state as ProductListUiState.Success) {
                assertEquals(2, this.products.size)
                assertEquals(1, this.categories.size)
                assertEquals(null, selectedCategoryId)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _03_ShouldEmitEmpty_WhenProductListIsEmpty() = runTest(testDispatcher) {
        every { getProductsUseCase(any(), any(), any(), any(), any(), any()) } returns
            flowOf(Result.success(emptyList()))
        every { getCategoriesUseCase() } returns
            flowOf(Result.success(listOf(fakeCategory(1L, "Electronics"))))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(ProductListUiState.Loading, awaitItem())
            val state = awaitItem()
            assertTrue(state is ProductListUiState.Empty)
            with(state as ProductListUiState.Empty) {
                assertEquals(1, categories.size)
                assertEquals("Electronics", categories[0].name)
                assertEquals(null, selectedCategoryId)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _04_ShouldEmitError_WhenUseCaseFailsAndProductsAreEmpty() = runTest(testDispatcher) {
        every { getProductsUseCase(any(), any(), any(), any(), any(), any()) } returns
            flowOf(Result.failure(RuntimeException("Network error")))
        every { getCategoriesUseCase() } returns
            flowOf(Result.success(emptyList()))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(ProductListUiState.Loading, awaitItem())
            val state = awaitItem()
            assertTrue(state is ProductListUiState.Error)
            assertEquals("Network error", (state as ProductListUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _05_ShouldTriggerNewCollectWithCategoryId_WhenSelectCategoryIsCalled() =
        runTest(testDispatcher) {
            val products = listOf(fakeProduct(1L, "iPhone", categoryId = 2L))
            every { getProductsUseCase(any(), any(), any(), any(), any(), any()) } returns
                flowOf(Result.success(products))
            every { getCategoriesUseCase() } returns
                flowOf(Result.success(emptyList()))

            val viewModel = createViewModel()

            viewModel.uiState.test {
                assertEquals(ProductListUiState.Loading, awaitItem())
                awaitItem() // initial Success with no filter

                viewModel.selectCategory(2L)
                awaitItem() // new emission after category change

                verify { getProductsUseCase(eq(2L), any(), any(), any(), any(), any()) }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun _06_ShouldTriggerNewCollectWithQuery_WhenSearchIsCalled() = runTest(testDispatcher) {
        val products = listOf(fakeProduct(1L, "iPhone"))
        every { getProductsUseCase(any(), any(), any(), any(), any(), any()) } returns
            flowOf(Result.success(products))
        every { getCategoriesUseCase() } returns
            flowOf(Result.success(emptyList()))

        val viewModel = createViewModel()

        // Advance past the first debounce (initial empty query) so the
        // ViewModel starts collecting. advanceTimeBy is only available on
        // TestCoroutineScheduler, not directly in the runTest lambda.
        testScheduler.advanceTimeBy(301L)
        testScheduler.advanceUntilIdle()

        // Now trigger the search and advance past the second debounce.
        viewModel.search("phone")
        testScheduler.advanceTimeBy(301L)
        testScheduler.advanceUntilIdle()

        // The use case must have been called with search = "phone".
        verify { getProductsUseCase(any(), eq("phone"), any(), any(), any(), any()) }
    }

    @Test
    fun _07_ShouldSetIsRefreshingTrueThenFalse_WhenRefreshIsCalled() = runTest(testDispatcher) {
        every { getProductsUseCase(any(), any(), any(), any(), any(), any()) } returns
            flowOf(Result.success(emptyList()))
        every { getCategoriesUseCase() } returns
            flowOf(Result.success(emptyList()))
        coEvery { refreshProductsUseCase(any()) } coAnswers { delay(1); Unit }

        val viewModel = createViewModel()

        viewModel.isRefreshing.test {
            assertFalse(awaitItem()) // initial false
            viewModel.refresh()
            assertTrue(awaitItem()) // set to true before refresh call
            assertFalse(awaitItem()) // set back to false after refresh completes
            cancelAndIgnoreRemainingEvents()
        }
    }
}
