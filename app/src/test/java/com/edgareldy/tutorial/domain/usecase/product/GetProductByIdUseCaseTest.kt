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
 * Unit tests for GetProductByIdUseCase.
 * Verifies that the use case delegates to ProductRepository.getProductById with
 * the correct id and propagates the result - success or failure - unchanged.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class GetProductByIdUseCaseTest {

    private lateinit var repository: ProductRepository
    private lateinit var useCase: GetProductByIdUseCase

    private val fakeProduct = Product(
        id = 42L,
        name = "iPhone 15",
        unitPrice = 999.99,
        description = "Latest smartphone",
        active = true,
        categoryId = 1L,
        categoryName = "Electronics",
        images = emptyList()
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetProductByIdUseCase(repository)
    }

    @Test
    fun _01_ShouldReturnSuccess_WhenProductExistsInRepository() = runTest {
        every { repository.getProductById(42L) } returns flowOf(Result.success(fakeProduct))

        val result = useCase(42L).first()

        assertTrue(result.isSuccess)
        assertEquals(fakeProduct, result.getOrNull())
        verify(exactly = 1) { repository.getProductById(42L) }
    }

    @Test
    fun _02_ShouldReturnFailure_WhenProductIsNotFoundInRepository() = runTest {
        every { repository.getProductById(99L) } returns
            flowOf(Result.failure(NoSuchElementException("Product 99 not found in cache")))

        val result = useCase(99L).first()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NoSuchElementException)
        verify(exactly = 1) { repository.getProductById(99L) }
    }

    @Test
    fun _03_ShouldDelegateCorrectId_WhenIdIsProvided() = runTest {
        every { repository.getProductById(7L) } returns flowOf(Result.success(fakeProduct.copy(id = 7L)))

        useCase(7L).first()

        verify(exactly = 1) { repository.getProductById(7L) }
        verify(exactly = 0) { repository.getProductById(42L) }
    }

    @Test
    fun _04_ShouldPropagateNetworkError_WhenRepositoryReturnsNetworkFailure() = runTest {
        every { repository.getProductById(any()) } returns
            flowOf(Result.failure(RuntimeException("Connection timeout")))

        val result = useCase(1L).first()

        assertTrue(result.isFailure)
        assertEquals("Connection timeout", result.exceptionOrNull()?.message)
    }
}
