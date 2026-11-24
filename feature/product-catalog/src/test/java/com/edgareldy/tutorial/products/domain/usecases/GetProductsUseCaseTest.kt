package com.edgareldy.tutorial.products.domain.usecases

import com.edgareldy.tutorial.products.domain.model.Product
import com.edgareldy.tutorial.products.domain.repository.ProductRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetProductsUseCase.
 * The use case delegates pagination and categoryId to ProductRepository, then
 * applies search query and price filters in the domain layer. These tests
 * verify that the filtering logic is correct and belongs here, not in the repo.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
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

    private fun fakeProduct(
        id: Long,
        name: String,
        unitPrice: Double = 10.0,
        categoryId: Long? = null
    ) = Product(
        id = id,
        name = name,
        unitPrice = unitPrice,
        description = null,
        active = true,
        categoryId = categoryId,
        categoryName = null,
        images = emptyList()
    )

    @Test
    fun _01_ShouldCallGetProductsWithCategoryIdAndPagination_WhenInvoked() = runTest {
        every { repository.getProducts(2L, 0, 20) } returns flowOf(Result.success(emptyList()))

        useCase(categoryId = 2L, page = 0, size = 20)

        verify(exactly = 1) { repository.getProducts(2L, 0, 20) }
    }

    @Test
    fun _02_ShouldReturnAllProducts_WhenNoFiltersAreApplied() = runTest {
        val products = listOf(
            fakeProduct(1L, "iPhone", unitPrice = 999.0),
            fakeProduct(2L, "MacBook", unitPrice = 1499.0)
        )
        every { repository.getProducts(null, 0, 20) } returns flowOf(Result.success(products))

        val result = useCase().single()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun _03_ShouldFilterBySearchQuery_WhenSearchIsProvided() = runTest {
        val products = listOf(
            fakeProduct(1L, "iPhone 15"),
            fakeProduct(2L, "MacBook Pro"),
            fakeProduct(3L, "iPhone 14")
        )
        every { repository.getProducts(null, 0, 20) } returns flowOf(Result.success(products))

        val result = useCase(search = "iPhone").single()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertTrue(result.getOrNull()!!.all { it.name.contains("iPhone", ignoreCase = true) })
    }

    @Test
    fun _04_ShouldFilterBySearchCaseInsensitively_WhenQueryHasUpperCase() = runTest {
        val products = listOf(
            fakeProduct(1L, "iphone 15"),
            fakeProduct(2L, "MacBook")
        )
        every { repository.getProducts(null, 0, 20) } returns flowOf(Result.success(products))

        val result = useCase(search = "IPHONE").single()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun _05_ShouldFilterByMinPrice_WhenMinPriceIsProvided() = runTest {
        val products = listOf(
            fakeProduct(1L, "Cheap", unitPrice = 5.0),
            fakeProduct(2L, "Mid", unitPrice = 50.0),
            fakeProduct(3L, "Expensive", unitPrice = 500.0)
        )
        every { repository.getProducts(null, 0, 20) } returns flowOf(Result.success(products))

        val result = useCase(minPrice = 50.0).single()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertTrue(result.getOrNull()!!.all { it.unitPrice >= 50.0 })
    }

    @Test
    fun _06_ShouldFilterByMaxPrice_WhenMaxPriceIsProvided() = runTest {
        val products = listOf(
            fakeProduct(1L, "Cheap", unitPrice = 5.0),
            fakeProduct(2L, "Mid", unitPrice = 50.0),
            fakeProduct(3L, "Expensive", unitPrice = 500.0)
        )
        every { repository.getProducts(null, 0, 20) } returns flowOf(Result.success(products))

        val result = useCase(maxPrice = 50.0).single()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertTrue(result.getOrNull()!!.all { it.unitPrice <= 50.0 })
    }

    @Test
    fun _07_ShouldApplyAllFiltersSimultaneously_WhenMultipleFiltersAreProvided() = runTest {
        val products = listOf(
            fakeProduct(1L, "iPhone 15", unitPrice = 999.0),
            fakeProduct(2L, "iPhone SE", unitPrice = 429.0),
            fakeProduct(3L, "MacBook Pro", unitPrice = 1499.0),
            fakeProduct(4L, "iPhone 14", unitPrice = 799.0)
        )
        every { repository.getProducts(null, 0, 20) } returns flowOf(Result.success(products))

        val result = useCase(search = "iPhone", minPrice = 500.0, maxPrice = 1000.0).single()

        assertTrue(result.isSuccess)
        val filtered = result.getOrNull()!!
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.name.contains("iPhone") && it.unitPrice in 500.0..1000.0 })
    }

    @Test
    fun _08_ShouldReturnEmptyList_WhenNoProductsMatchFilters() = runTest {
        val products = listOf(fakeProduct(1L, "MacBook", unitPrice = 1499.0))
        every { repository.getProducts(null, 0, 20) } returns flowOf(Result.success(products))

        val result = useCase(search = "iPhone").single()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun _09_ShouldPropagateRepositoryFailure_WhenRepositoryFails() = runTest {
        every { repository.getProducts(null, 0, 20) } returns
            flowOf(Result.failure(RuntimeException("Network error")))

        val result = useCase().single()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}
