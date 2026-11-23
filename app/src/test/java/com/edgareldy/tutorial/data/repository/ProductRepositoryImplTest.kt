package com.edgareldy.tutorial.data.repository

import com.edgareldy.tutorial.data.local.dao.ProductDao
import com.edgareldy.tutorial.data.local.entity.ProductEntity
import com.edgareldy.tutorial.data.local.entity.ProductWithImages
import com.edgareldy.tutorial.data.remote.api.ProductApiService
import com.edgareldy.tutorial.data.remote.dto.ApiResponse
import com.edgareldy.tutorial.data.remote.dto.PageResponse
import com.edgareldy.tutorial.data.remote.dto.ProductResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
 * Unit tests for ProductRepositoryImpl using MockK.
 * Verifies the cache-first strategy: Room emits first, then a background refresh
 * triggers via onStart. The routing between getProducts() and getProductsByCategoryId()
 * is also covered. Filtering (search, price range) lives in the use case, not here.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProductRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var productApiService: ProductApiService
    private lateinit var productDao: ProductDao
    private lateinit var repository: ProductRepositoryImpl

    @Before
    fun setUp() {
        productApiService = mockk()
        productDao = mockk()
        repository = ProductRepositoryImpl(productApiService, productDao, testScope)
    }

    // --- Helpers ---

    private fun fakeProductEntity(id: Long, name: String, categoryId: Long? = null) =
        ProductEntity(
            id = id,
            productName = name,
            unitPrice = 9.99,
            description = null,
            active = true,
            categoryId = categoryId,
            categoryName = null
        )

    private fun fakeProductWithImages(id: Long, name: String, categoryId: Long? = null) =
        ProductWithImages(
            product = fakeProductEntity(id, name, categoryId),
            images = emptyList()
        )

    private fun fakeProductResponse(id: Long, name: String, categoryId: Long? = null) =
        ProductResponse(
            id = id,
            productName = name,
            unitPrice = 9.99,
            categoryId = categoryId,
            images = emptyList()
        )

    private fun emptyApiPageResponse() =
        ApiResponse(
            success = true,
            data = PageResponse<ProductResponse>(content = emptyList())
        )

    // --- Tests ---

    @Test
    fun _01_ShouldEmitSuccessWithEmptyList_WhenRoomIsEmpty() = runTest(testDispatcher) {
        every { productDao.getProducts() } returns flowOf(emptyList())
        coEvery { productApiService.getProducts(any(), any(), any(), any(), any(), any()) } returns
            emptyApiPageResponse()
        coEvery { productDao.upsertAll(any()) } returns Unit

        val result = repository.getProducts().first()

        testScope.advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals(emptyList<Any>(), result.getOrNull())
    }

    @Test
    fun _02_ShouldEmitSuccessWithProducts_WhenRoomHasData() = runTest(testDispatcher) {
        val rows = listOf(
            fakeProductWithImages(1L, "iPhone"),
            fakeProductWithImages(2L, "MacBook")
        )
        every { productDao.getProducts() } returns flowOf(rows)
        coEvery { productApiService.getProducts(any(), any(), any(), any(), any(), any()) } returns
            emptyApiPageResponse()
        coEvery { productDao.upsertAll(any()) } returns Unit

        val result = repository.getProducts().first()

        testScope.advanceUntilIdle()

        assertTrue(result.isSuccess)
        val products = result.getOrNull()!!
        assertEquals(2, products.size)
        assertEquals("iPhone", products[0].name)
        assertEquals("MacBook", products[1].name)
    }

    @Test
    fun _03_ShouldCallGetProductsByCategoryId_WhenCategoryIdIsProvided() =
        runTest(testDispatcher) {
            every { productDao.getProductsByCategoryId(5L) } returns flowOf(emptyList())
            coEvery { productApiService.getProducts(any(), any(), any(), any(), any(), any()) } returns
                emptyApiPageResponse()
            coEvery { productDao.upsertAll(any()) } returns Unit

            repository.getProducts(categoryId = 5L).first()

            testScope.advanceUntilIdle()

            verify(exactly = 1) { productDao.getProductsByCategoryId(5L) }
            verify(exactly = 0) { productDao.getProducts() }
        }

    @Test
    fun _04_ShouldCallApiAndUpsertEntities_WhenRefreshProductsIsCalled() =
        runTest(testDispatcher) {
            val dtos = listOf(fakeProductResponse(1L, "iPhone"))
            coEvery {
                productApiService.getProducts(
                    categoryId = null,
                    productName = null,
                    minPrice = null,
                    maxPrice = null,
                    page = 0,
                    size = 100
                )
            } returns ApiResponse(
                success = true,
                data = PageResponse(content = dtos)
            )
            coEvery { productDao.upsertAll(any()) } returns Unit

            repository.refreshProducts(null)

            coVerify(exactly = 1) {
                productApiService.getProducts(
                    categoryId = null,
                    productName = null,
                    minPrice = null,
                    maxPrice = null,
                    page = 0,
                    size = 100
                )
            }
            coVerify(exactly = 1) { productDao.upsertAll(any()) }
        }

    @Test
    fun _05_ShouldEmitError_WhenProductByIdIsNotFoundInCache() = runTest(testDispatcher) {
        every { productDao.getProductById(99L) } returns flowOf(null)
        coEvery { productApiService.getProductById(99L) } returns
            ApiResponse(success = false, data = null)

        val result = repository.getProductById(99L).first()

        testScope.advanceUntilIdle()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NoSuchElementException)
        assertEquals("Product 99 not found in cache", result.exceptionOrNull()?.message)
    }

    @Test
    fun _06_ShouldReturnSuccess_WhenProductByIdIsFoundInCache() = runTest(testDispatcher) {
        val row = fakeProductWithImages(42L, "Samsung Galaxy")
        every { productDao.getProductById(42L) } returns flowOf(row)
        coEvery { productApiService.getProductById(42L) } returns
            ApiResponse(
                success = true,
                data = fakeProductResponse(42L, "Samsung Galaxy")
            )
        coEvery { productDao.upsertAll(any()) } returns Unit

        val result = repository.getProductById(42L).first()

        testScope.advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals(42L, result.getOrNull()?.id)
        assertEquals("Samsung Galaxy", result.getOrNull()?.name)
    }
}
