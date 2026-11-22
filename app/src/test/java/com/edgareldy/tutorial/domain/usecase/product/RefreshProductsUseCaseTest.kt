package com.edgareldy.tutorial.domain.usecase.product

import com.edgareldy.tutorial.domain.repository.ProductRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for RefreshProductsUseCase.
 * Verifies that the use case delegates the refresh call to ProductRepository
 * with the correct optional categoryId parameter.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class RefreshProductsUseCaseTest {

    private lateinit var repository: ProductRepository
    private lateinit var useCase: RefreshProductsUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = RefreshProductsUseCase(repository)
    }

    @Test
    fun _01_ShouldCallRefreshWithNull_WhenInvokedWithNoArgs() = runTest {
        coEvery { repository.refreshProducts(null) } returns Unit

        useCase()

        coVerify(exactly = 1) { repository.refreshProducts(null) }
    }

    @Test
    fun _02_ShouldCallRefreshWithCategoryId_WhenCategoryIdIsProvided() = runTest {
        coEvery { repository.refreshProducts(5L) } returns Unit

        useCase(categoryId = 5L)

        coVerify(exactly = 1) { repository.refreshProducts(5L) }
    }

    @Test
    fun _03_ShouldCallRefreshOnce_WhenInvokedOnce() = runTest {
        coEvery { repository.refreshProducts(any()) } returns Unit

        useCase()

        coVerify(exactly = 1) { repository.refreshProducts(any()) }
    }

    @Test
    fun _04_ShouldNotCallRefreshWithAnotherCategoryId_WhenSpecificIdIsProvided() = runTest {
        coEvery { repository.refreshProducts(any()) } returns Unit

        useCase(categoryId = 3L)

        coVerify(exactly = 1) { repository.refreshProducts(3L) }
        coVerify(exactly = 0) { repository.refreshProducts(1L) }
        coVerify(exactly = 0) { repository.refreshProducts(null) }
    }
}
