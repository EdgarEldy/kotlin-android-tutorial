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
 * Verifies that filtering (search, price range) is applied in the use case layer,
 * not delegated to the repository. The repository contract now has only categoryId,
 * page and size; all other filters are domain logic owned by this use case.
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
        every { repository.getProducts(any(), any(), any()) } returns
            flowOf(Result.success(emptyList()))

        val result = useCase().first()

        assertTrue(result.isSuccess)
        assertEquals(emptyList<Product>(), result.getOrNull())
    }

    @Test
    fun _02_ShouldReturnSuccessWithProducts_WhenRepositoryReturnsList() = runTest {
        val products = listOf(fakeProduct(1L, "iPhone"), fakeProduct(2L, "MacBook"))
        every { repository.getProducts(any(), any(), any()) } returns
            flowOf(Result.success(products))

        val result = useCase().first()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("iPhone", result.getOrNull()?.first()?.name)
    }

    @Test
    fun _03_ShouldPassCategoryIdToRepository_WhenCategoryIdIsProvided() = runTest {
        every { repository.getProducts(any(), any(), any()) } returns
            flowOf(Result.success(emptyList()))

        useCase(categoryId = 5L).first()

        verify(exactly = 1) { repository.getProducts(5L, 0, 20) }
    }

    @Test
    fun _04_ShouldFilterProductsByName_WhenSearchQueryIsProvided() = runTest {
        val products = listOf(fakeProduct(1L, "iPhone 15"), fakeProduct(2L, "MacBook Pro"))
        every { repository.getProducts(any(), any(), any()) } returns
            flowOf(Result.success(products))

        val result = useCase(search = "iphone").first()

        assertTrue(result.isSuccess)
        val filtered = result.getOrNull()!!
        assertEquals(1, filtered.size)
        assertEquals("iPhone 15", filtered[0].name)
    }

    @Test
    fun _05_ShouldFilterProductsByPrice_WhenMinAndMaxPricesAreProvided() = runTest {
        val products = listOf(
            fakeProduct(1L, "Budget Phone", unitPrice = 50.0),
            fakeProduct(2L, "Mid Phone", unitPrice = 300.0),
            fakeProduct(3L, "Premium Phone", unitPrice = 900.0)
        )
        every { repository.getProducts(any(), any(), any()) } returns
            flowOf(Result.success(products))

        val result = useCase(minPrice = 100.0, maxPrice = 500.0).first()

        assertTrue(result.isSuccess)
        val filtered = result.getOrNull()!!
        assertEquals(1, filtered.size)
        assertEquals("Mid Phone", filtered[0].name)
    }

    @Test
    fun _06_ShouldReturnFailure_WhenRepositoryReturnsFailure() = runTest {
        every { repository.getProducts(any(), any(), any()) } returns
            flowOf(Result.failure(RuntimeException("Network error")))

        val result = useCase().first()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    private fun fakeProduct(id: Long, name: String, unitPrice: Double = 9.99) = Product(
        id = id,
        name = name,
        unitPrice = unitPrice,
        description = null,
        active = true,
        categoryId = null,
        categoryName = null,
        images = emptyList()
    )
}
