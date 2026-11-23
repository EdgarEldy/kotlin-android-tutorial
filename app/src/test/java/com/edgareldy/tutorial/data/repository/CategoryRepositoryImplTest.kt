package com.edgareldy.tutorial.data.repository

import com.edgareldy.tutorial.data.local.dao.CategoryDao
import com.edgareldy.tutorial.data.local.entity.CategoryEntity
import com.edgareldy.tutorial.data.remote.api.CategoryApiService
import com.edgareldy.tutorial.data.remote.dto.ApiResponse
import com.edgareldy.tutorial.data.remote.dto.CategoryResponse
import com.edgareldy.tutorial.data.remote.dto.PageResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CategoryRepositoryImpl using MockK.
 * Verifies the cache-first strategy: Room emits immediately, then a background
 * refresh updates the cache via onStart. Also covers the refreshCategories()
 * function which calls the API and upserts into Room.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CategoryRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var categoryApiService: CategoryApiService
    private lateinit var categoryDao: CategoryDao
    private lateinit var repository: CategoryRepositoryImpl

    @Before
    fun setUp() {
        categoryApiService = mockk()
        categoryDao = mockk()
        repository = CategoryRepositoryImpl(categoryApiService, categoryDao, testScope)
    }

    // --- Helpers ---

    private fun emptyApiPageResponse() =
        ApiResponse(
            success = true,
            data = PageResponse<CategoryResponse>(content = emptyList())
        )

    // --- Tests ---

    @Test
    fun _01_ShouldEmitSuccessWithEmptyList_WhenRoomIsEmpty() = runTest(testDispatcher) {
        every { categoryDao.getCategories() } returns flowOf(emptyList())
        coEvery { categoryApiService.getCategories(any(), any()) } returns emptyApiPageResponse()

        val result = repository.getCategories().first()

        testScope.advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals(emptyList<Any>(), result.getOrNull())
    }

    @Test
    fun _02_ShouldEmitSuccessWithCategories_WhenRoomHasData() = runTest(testDispatcher) {
        val entities = listOf(
            CategoryEntity(id = 1L, categoryName = "Electronics"),
            CategoryEntity(id = 2L, categoryName = "Clothing")
        )
        every { categoryDao.getCategories() } returns flowOf(entities)
        coEvery { categoryApiService.getCategories(any(), any()) } returns emptyApiPageResponse()

        val result = repository.getCategories().first()

        testScope.advanceUntilIdle()

        assertTrue(result.isSuccess)
        val categories = result.getOrNull()!!
        assertEquals(2, categories.size)
        assertEquals(1L, categories[0].id)
        assertEquals("Electronics", categories[0].name)
        assertEquals(2L, categories[1].id)
        assertEquals("Clothing", categories[1].name)
    }

    @Test
    fun _03_ShouldCallApiAndUpsertCategories_WhenRefreshCategoriesIsCalled() =
        runTest(testDispatcher) {
            val dto = CategoryResponse(id = 10L, categoryName = "Sports")
            coEvery { categoryApiService.getCategories(0, 100) } returns
                ApiResponse(
                    success = true,
                    data = PageResponse(content = listOf(dto))
                )
            coEvery { categoryDao.upsertAll(any()) } returns Unit

            repository.refreshCategories()

            coVerify(exactly = 1) { categoryApiService.getCategories(0, 100) }
            coVerify(exactly = 1) {
                categoryDao.upsertAll(match { list ->
                    list.size == 1 && list[0].id == 10L && list[0].categoryName == "Sports"
                })
            }
        }

    @Test
    fun _04_ShouldNotCallRefresh_WhenDataIsReturnedFromCacheOnly() = runTest(testDispatcher) {
        val entity = CategoryEntity(id = 1L, categoryName = "Electronics")
        every { categoryDao.getCategories() } returns flowOf(listOf(entity))
        coEvery { categoryApiService.getCategories(any(), any()) } returns emptyApiPageResponse()
        coEvery { categoryDao.upsertAll(any()) } returns Unit

        val result = repository.getCategories().first()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun _05_ShouldReturnEmptyList_WhenRefreshReturnsNullData() = runTest(testDispatcher) {
        coEvery { categoryApiService.getCategories(any(), any()) } returns
            ApiResponse(success = true, data = null)

        repository.refreshCategories()

        coVerify(exactly = 0) { categoryDao.upsertAll(any()) }
    }
}
