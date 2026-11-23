package com.edgareldy.tutorial.domain.usecase.cart

import com.edgareldy.tutorial.domain.model.Cart
import com.edgareldy.tutorial.domain.model.CartItem
import com.edgareldy.tutorial.domain.repository.CartRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for AddCartItemUseCase.
 * Verifies input validation (quantity < 1) and delegation to CartRepository
 * when the quantity is valid. The repository is never called when validation fails.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class AddCartItemUseCaseTest {

    private lateinit var repository: CartRepository
    private lateinit var useCase: AddCartItemUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = AddCartItemUseCase(repository)
    }

    // --- Helpers ---

    private fun fakeCart(vararg items: CartItem) = Cart(
        id = 1L,
        items = items.toList(),
        total = items.sumOf { it.subtotal }
    )

    private fun fakeItem(id: Long, quantity: Int) = CartItem(
        id = id,
        productId = 10L,
        productName = "Product $id",
        unitPrice = 9.99,
        quantity = quantity,
        subtotal = 9.99 * quantity
    )

    // --- Tests ---

    @Test
    fun _01_ShouldReturnSuccess_WhenQuantityIsOne() = runTest {
        val cart = fakeCart(fakeItem(1L, 1))
        coEvery { repository.addItem(1L, 1) } returns Result.success(cart)

        val result = useCase(productId = 1L, quantity = 1)

        assertTrue(result.isSuccess)
        assertEquals(cart, result.getOrNull())
        coVerify(exactly = 1) { repository.addItem(1L, 1) }
    }

    @Test
    fun _02_ShouldReturnSuccess_WhenQuantityIsGreaterThanOne() = runTest {
        val cart = fakeCart(fakeItem(1L, 3))
        coEvery { repository.addItem(1L, 3) } returns Result.success(cart)

        val result = useCase(productId = 1L, quantity = 3)

        assertTrue(result.isSuccess)
        assertEquals(cart, result.getOrNull())
        coVerify(exactly = 1) { repository.addItem(1L, 3) }
    }

    @Test
    fun _03_ShouldReturnFailure_WhenQuantityIsZero() = runTest {
        val result = useCase(productId = 1L, quantity = 0)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Quantity must be at least 1", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { repository.addItem(any(), any()) }
    }

    @Test
    fun _04_ShouldReturnFailure_WhenQuantityIsNegative() = runTest {
        val result = useCase(productId = 1L, quantity = -1)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Quantity must be at least 1", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { repository.addItem(any(), any()) }
    }

    @Test
    fun _05_ShouldPropagateRepositoryFailure_WhenNetworkErrorOccurs() = runTest {
        val networkError = RuntimeException("Network unavailable")
        coEvery { repository.addItem(1L, 2) } returns Result.failure(networkError)

        val result = useCase(productId = 1L, quantity = 2)

        assertTrue(result.isFailure)
        assertEquals("Network unavailable", result.exceptionOrNull()?.message)
    }
}
