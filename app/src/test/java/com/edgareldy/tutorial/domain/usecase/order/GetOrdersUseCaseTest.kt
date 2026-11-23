package com.edgareldy.tutorial.domain.usecase.order

import com.edgareldy.tutorial.domain.model.Order
import com.edgareldy.tutorial.domain.model.OrderStatus
import com.edgareldy.tutorial.domain.repository.OrderRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetOrdersUseCase.
 * Verifies that the use case wraps the repository flow correctly and
 * forwards both non-empty and empty result lists without modification.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class GetOrdersUseCaseTest {

    private lateinit var mockRepository: OrderRepository
    private lateinit var getOrdersUseCase: GetOrdersUseCase

    // --- Helpers ---

    private fun fakeOrder(id: Long = 1L) = Order(
        id = id,
        orderNumber = "ORD-$id",
        status = OrderStatus.PENDING,
        totalAmount = 29.99,
        items = emptyList()
    )

    @Before
    fun setUp() {
        mockRepository = mockk()
        getOrdersUseCase = GetOrdersUseCase(mockRepository)
    }

    // --- Tests ---

    @Test
    fun `invoke emits success with order list when repository returns non-empty list`() = runTest {
        // given
        val orders = listOf(fakeOrder(1L), fakeOrder(2L))
        every { mockRepository.getOrders(0, 20) } returns flowOf(Result.success(orders))

        // when
        val collected = mutableListOf<Result<List<Order>>>()
        getOrdersUseCase().collect { collected.add(it) }

        // then
        assertEquals(1, collected.size)
        assertTrue(collected[0].isSuccess)
        assertEquals(orders, collected[0].getOrNull())
        verify(exactly = 1) { mockRepository.getOrders(0, 20) }
    }

    @Test
    fun `invoke emits success with empty list when repository returns empty list`() = runTest {
        // given
        every { mockRepository.getOrders(0, 20) } returns flowOf(Result.success(emptyList()))

        // when
        val collected = mutableListOf<Result<List<Order>>>()
        getOrdersUseCase().collect { collected.add(it) }

        // then
        assertEquals(1, collected.size)
        assertTrue(collected[0].isSuccess)
        assertTrue(collected[0].getOrNull()!!.isEmpty())
        verify(exactly = 1) { mockRepository.getOrders(0, 20) }
    }
}
