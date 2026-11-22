package com.edgareldy.tutorial.domain.usecase.product

import com.edgareldy.tutorial.domain.model.Product
import com.edgareldy.tutorial.domain.repository.ProductRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetProductsUseCase.
 * Verifies that the use case delegates all calls to ProductRepository with the
 * correct parameters, and that it propagates both success and failure results
 * unchanged to the caller.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class GetProductsUseCaseTest {

    private lateinit var repository: ProductRepository
    private lateinit var useCase: GetProductsUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetProductsUseCase(repository)
    }

    @Test
    fun _01_ShouldReturnSuccessWithEmptyList_WhenRepositoryReturnsEmptyList() = runTest {
        every { repository.getProducts(any(), any(), any(), any(), any(), any()) } returns
            flowOf(Result.success(emptyList()))

        val result = useCase().first()

        assertTrue(result.isSuccess)
        assertEquals(emptyList<Product>(), result.getOrNull())
    }

    @Test
    fun _02_ShouldReturnSuccessWithProducts_WhenRepositoryReturnsList() = runTest {
        val products = listOf(fakeProduct(1L, "iPhone"), fakeProduct(2L, "MacBook"))
        every { repository.getProducts(any(), any(), any(), any(), any(), any()) } returns
            flowOf(Result.success(products))

        val result = useCase().first()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("iPhone", result.getOrNull()?.first()?.name)
    }

    @Test
    fun _03_ShouldDelegateCategoryId_WhenCategoryIdIsProvided() = runTest {
        every { repository.getProducts(any(), any(), any(), any(), any(), any()) } returns
            flowOf(Result.success(emptyList()))

        useCase(categoryId = 5L).first()

        verify(exactly = 1) { repository.getProducts(5L, null, null, null, 0, 20) }
    }

    @Test
    fun _04_ShouldDelegateSearchQuery_WhenSearchIsProvided() = runTest {
        every { repository.getProducts(any(), any(), any(), any(), any(), any()) } returns
            flowOf(Result.success(emptyList()))

        useCase(search = "phone").first()

        verify(exactly = 1) { repository.getProducts(null, "phone", null, null, 0, 20) }
    }

    @Test
    fun _05_ShouldDelegatePriceRange_WhenMinAndMaxPricesAreProvided() = runTest {
        every { repository.getProducts(any(), any(), any(), any(), any(), any()) } returns
            flowOf(Result.success(emptyList()))

        useCase(minPrice = 100.0, maxPrice = 500.0).first()

        verify(exactly = 1) { repository.getProducts(null, null, 100.0, 500.0, 0, 20) }
    }

    @Test
    fun _06_ShouldReturnFailure_WhenRepositoryReturnsFailure() = runTest {
        every { repository.getProducts(any(), any(), any(), any(), any(), any()) } returns
            flowOf(Result.failure(RuntimeException("Network error")))

        val result = useCase().first()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    private fun fakeProduct(id: Long, name: String) = Product(
        id = id,
        name = name,
        unitPrice = 9.99,
        description = null,
        active = true,
        categoryId = null,
        categoryName = null,
        images = emptyList()
    )
}
