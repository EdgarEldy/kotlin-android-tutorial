package com.edgareldy.tutorial.domain.usecase.cart

import com.edgareldy.tutorial.domain.model.Cart
import com.edgareldy.tutorial.domain.model.CartItem
import com.edgareldy.tutorial.domain.repository.CartRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetCartItemCountUseCase.
 * Verifies the badge count logic: sum of all item quantities, 0 for an empty cart,
 * and 0 on repository error so the badge disappears gracefully.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class GetCartItemCountUseCaseTest {

    private lateinit var repository: CartRepository
    private lateinit var useCase: GetCartItemCountUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetCartItemCountUseCase(repository)
    }

    // --- Helpers ---

    private fun fakeItem(id: Long, quantity: Int) = CartItem(
        id = id,
        productId = 10L + id,
        productName = "Product $id",
        unitPrice = 9.99,
        quantity = quantity,
        subtotal = 9.99 * quantity
    )

    private fun fakeCart(vararg items: CartItem) = Cart(
        id = 1L,
        items = items.toList(),
        total = items.sumOf { it.subtotal }
    )

    // --- Tests ---

    @Test
    fun _01_ShouldReturnSumOfQuantities_WhenCartHasMultipleItems() = runTest {
        val cart = fakeCart(fakeItem(1L, 2), fakeItem(2L, 3))
        coEvery { repository.getCart() } returns Result.success(cart)

        val count = useCase()

        assertEquals(5, count)
    }

    @Test
    fun _02_ShouldReturnZero_WhenCartIsEmpty() = runTest {
        val emptyCart = fakeCart()
        coEvery { repository.getCart() } returns Result.success(emptyCart)

        val count = useCase()

        assertEquals(0, count)
    }

    @Test
    fun _03_ShouldReturnZero_WhenRepositoryReturnsFailure() = runTest {
        coEvery { repository.getCart() } returns Result.failure(RuntimeException("Network error"))

        val count = useCase()

        assertEquals(0, count)
    }

    @Test
    fun _04_ShouldReturnExactQuantity_WhenCartHasOneItem() = runTest {
        val cart = fakeCart(fakeItem(1L, 7))
        coEvery { repository.getCart() } returns Result.success(cart)

        val count = useCase()

        assertEquals(7, count)
    }
}
