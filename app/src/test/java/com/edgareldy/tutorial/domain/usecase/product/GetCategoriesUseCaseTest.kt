package com.edgareldy.tutorial.domain.usecase.product

import com.edgareldy.tutorial.domain.model.Category
import com.edgareldy.tutorial.domain.repository.CategoryRepository
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
 * Unit tests for GetCategoriesUseCase.
 * Verifies that the use case is a pure delegate to CategoryRepository.getCategories
 * and correctly propagates both non-empty lists and failures.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
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
    fun _01_ShouldReturnEmptyList_WhenRepositoryReturnsEmptyList() = runTest {
        every { repository.getCategories() } returns flowOf(Result.success(emptyList()))

        val result = useCase().first()

        assertTrue(result.isSuccess)
        assertEquals(emptyList<Category>(), result.getOrNull())
        verify(exactly = 1) { repository.getCategories() }
    }

    @Test
    fun _02_ShouldReturnCategories_WhenRepositoryReturnsList() = runTest {
        val categories = listOf(
            Category(id = 1L, name = "Electronics"),
            Category(id = 2L, name = "Clothing")
        )
        every { repository.getCategories() } returns flowOf(Result.success(categories))

        val result = useCase().first()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("Electronics", result.getOrNull()?.first()?.name)
    }

    @Test
    fun _03_ShouldReturnFailure_WhenRepositoryReturnsFailure() = runTest {
        every { repository.getCategories() } returns
            flowOf(Result.failure(RuntimeException("Failed to load categories")))

        val result = useCase().first()

        assertTrue(result.isFailure)
        assertEquals("Failed to load categories", result.exceptionOrNull()?.message)
    }

    @Test
    fun _04_ShouldDelegateToRepository_WithoutAnyTransformation() = runTest {
        val category = Category(id = 10L, name = "Sports")
        every { repository.getCategories() } returns flowOf(Result.success(listOf(category)))

        val result = useCase().first()

        assertEquals(category, result.getOrNull()?.first())
        verify(exactly = 1) { repository.getCategories() }
    }
}
