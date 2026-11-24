package com.edgareldy.tutorial.orders.domain.usecases

import com.edgareldy.tutorial.orders.domain.model.Order
import com.edgareldy.tutorial.orders.domain.model.OrderItem
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
 * Unit tests for GetOrdersUseCase.
 * Verifies that invoke() correctly delegates to OrderRepository.getOrders()
 * and transparently returns the Flow emitted by the repository.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
class GetOrdersUseCaseTest {

    private lateinit var repository: OrderRepository
    private lateinit var useCase: GetOrdersUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetOrdersUseCase(repository)
    }

    private fun fakeOrder(id: Long) = Order(
        id = id,
        orderNumber = "ORD-$id",
        status = OrderStatus.PENDING,
        totalAmount = 99.0,
        items = emptyList()
    )

    @Test
    fun _01_ShouldDelegateToRepository_WhenInvoked() = runTest {
        every { repository.getOrders() } returns flowOf(Result.success(emptyList()))

        useCase()

        verify(exactly = 1) { repository.getOrders() }
    }

    @Test
    fun _02_ShouldReturnRepositoryFlow_WhenUseCaseIsInvoked() = runTest {
        val orders = listOf(fakeOrder(1L), fakeOrder(2L))
        every { repository.getOrders() } returns flowOf(Result.success(orders))

        val result = useCase().single()

        assertTrue(result.isSuccess)
        assertEquals(orders, result.getOrNull())
    }

    @Test
    fun _03_ShouldPropagateRepositoryFailure_WhenRepositoryFails() = runTest {
        every { repository.getOrders() } returns
            flowOf(Result.failure(RuntimeException("Network error")))

        val result = useCase().single()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}
