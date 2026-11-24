package com.edgareldy.tutorial.products.domain.usecases

import com.edgareldy.tutorial.products.domain.model.Category
import com.edgareldy.tutorial.products.domain.repository.CategoryRepository
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
 * Unit tests for GetCategoriesUseCase.
 * Verifies that the use case delegates to CategoryRepository without
 * adding any business logic, and passes the returned Flow as-is.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
class GetCategoriesUseCaseTest {

    private lateinit var repository: CategoryRepository
    private lateinit var useCase: GetCategoriesUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetCategoriesUseCase(repository)
    }

    @Test
    fun _01_ShouldCallGetCategories_WhenInvoked() = runTest {
        every { repository.getCategories() } returns flowOf(Result.success(emptyList()))

        useCase()

        verify(exactly = 1) { repository.getCategories() }
    }

    @Test
    fun _02_ShouldReturnCategoriesFromRepository_WhenRepositorySucceeds() = runTest {
        val categories = listOf(
            Category(id = 1L, name = "Electronics"),
            Category(id = 2L, name = "Clothing")
        )
        every { repository.getCategories() } returns flowOf(Result.success(categories))

        val result = useCase().single()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("Electronics", result.getOrNull()?.first()?.name)
    }

    @Test
    fun _03_ShouldReturnFailure_WhenRepositoryFails() = runTest {
        every { repository.getCategories() } returns
            flowOf(Result.failure(RuntimeException("DB error")))

        val result = useCase().single()

        assertTrue(result.isFailure)
        assertEquals("DB error", result.exceptionOrNull()?.message)
    }

    @Test
    fun _04_ShouldReturnEmptyList_WhenRepositoryReturnsNoCategories() = runTest {
        every { repository.getCategories() } returns flowOf(Result.success(emptyList()))

        val result = useCase().single()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }
}
