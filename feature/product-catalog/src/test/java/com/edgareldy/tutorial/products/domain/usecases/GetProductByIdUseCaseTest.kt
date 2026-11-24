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
 * Unit tests for GetProductByIdUseCase.
 * Verifies delegation to ProductRepository.getProductById with the correct id,
 * and that the returned Flow is not modified by the use case.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
class GetProductByIdUseCaseTest {

    private lateinit var repository: ProductRepository
    private lateinit var useCase: GetProductByIdUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetProductByIdUseCase(repository)
    }

    private fun fakeProduct(id: Long) = Product(
        id = id,
        name = "Product $id",
        unitPrice = 49.99,
        description = "Description",
        active = true,
        categoryId = 1L,
        categoryName = "Category",
        images = emptyList()
    )

    @Test
    fun _01_ShouldCallGetProductByIdWithCorrectId_WhenInvoked() = runTest {
        every { repository.getProductById(42L) } returns flowOf(Result.success(fakeProduct(42L)))

        useCase(42L)

        verify(exactly = 1) { repository.getProductById(42L) }
    }

    @Test
    fun _02_ShouldReturnProductFromRepository_WhenRepositorySucceeds() = runTest {
        every { repository.getProductById(1L) } returns flowOf(Result.success(fakeProduct(1L)))

        val result = useCase(1L).single()

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull()?.id)
        assertEquals("Product 1", result.getOrNull()?.name)
    }

    @Test
    fun _03_ShouldReturnFailure_WhenProductIsNotFound() = runTest {
        every { repository.getProductById(99L) } returns
            flowOf(Result.failure(NoSuchElementException("Product not found")))

        val result = useCase(99L).single()

        assertTrue(result.isFailure)
        assertEquals("Product not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun _04_ShouldNotCallRepositoryWithWrongId_WhenSpecificIdIsProvided() = runTest {
        every { repository.getProductById(5L) } returns flowOf(Result.success(fakeProduct(5L)))

        useCase(5L)

        verify(exactly = 0) { repository.getProductById(1L) }
        verify(exactly = 0) { repository.getProductById(99L) }
    }
}
