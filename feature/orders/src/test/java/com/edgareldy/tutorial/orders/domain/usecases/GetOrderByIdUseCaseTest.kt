package com.edgareldy.tutorial.orders.domain.usecases

import com.edgareldy.tutorial.orders.domain.model.Order
import com.edgareldy.tutorial.orders.domain.model.OrderStatus
import com.edgareldy.tutorial.orders.domain.repository.OrderRepository
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
 * Unit tests for GetOrderByIdUseCase.
 * Verifies that invoke(id) forwards the id to OrderRepository.getOrderById()
 * and returns the Flow as-is, including error cases.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
class GetOrderByIdUseCaseTest {

    private lateinit var repository: OrderRepository
    private lateinit var useCase: GetOrderByIdUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetOrderByIdUseCase(repository)
    }

    private fun fakeOrder(id: Long) = Order(
        id = id,
        orderNumber = "ORD-$id",
        status = OrderStatus.CONFIRMED,
        totalAmount = 149.99,
        items = emptyList()
    )

    @Test
    fun _01_ShouldDelegateToRepositoryWithCorrectId_WhenInvoked() = runTest {
        every { repository.getOrderById(42L) } returns flowOf(Result.success(fakeOrder(42L)))

        useCase(42L)

        verify(exactly = 1) { repository.getOrderById(42L) }
    }

    @Test
    fun _02_ShouldReturnOrderFromRepository_WhenIdExists() = runTest {
        val order = fakeOrder(10L)
        every { repository.getOrderById(10L) } returns flowOf(Result.success(order))

        val result = useCase(10L).single()

        assertTrue(result.isSuccess)
        assertEquals(order, result.getOrNull())
    }

    @Test
    fun _03_ShouldPropagateFailure_WhenOrderIsNotFound() = runTest {
        every { repository.getOrderById(99L) } returns
            flowOf(Result.failure(RuntimeException("Order not found")))

        val result = useCase(99L).single()

        assertTrue(result.isFailure)
        assertEquals("Order not found", result.exceptionOrNull()?.message)
    }
}
