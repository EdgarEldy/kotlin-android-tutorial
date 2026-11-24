package com.edgareldy.tutorial.orders.domain.usecases

import com.edgareldy.tutorial.cart.domain.usecases.ClearCartUseCase
import com.edgareldy.tutorial.orders.domain.model.Order
import com.edgareldy.tutorial.orders.domain.model.OrderStatus
import com.edgareldy.tutorial.orders.domain.repository.OrderRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CreateOrderUseCase.
 * Covers the success path (backend creates order from cart) and the network
 * failure path, verifying that Result wrapping is preserved end-to-end.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
class CreateOrderUseCaseTest {

    private lateinit var repository: OrderRepository
    private lateinit var clearCartUseCase: ClearCartUseCase
    private lateinit var useCase: CreateOrderUseCase

    @Before
    fun setUp() {
        repository = mockk()
        clearCartUseCase = mockk()
        coJustRun { clearCartUseCase() }
        useCase = CreateOrderUseCase(repository, clearCartUseCase)
    }

    private fun fakeOrder(id: Long) = Order(
        id = id,
        orderNumber = "ORD-$id",
        status = OrderStatus.PENDING,
        totalAmount = 250.0,
        items = emptyList()
    )

    @Test
    fun _01_ShouldReturnSuccess_WhenRepositoryCreatesOrder() = runTest {
        val order = fakeOrder(1L)
        coEvery { repository.createOrder() } returns Result.success(order)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(order, result.getOrNull())
        coVerify(exactly = 1) { repository.createOrder() }
    }

    @Test
    fun _02_ShouldReturnFailure_WhenNetworkErrorOccurs() = runTest {
        val exception = RuntimeException("No internet connection")
        coEvery { repository.createOrder() } returns Result.failure(exception)

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals("No internet connection", result.exceptionOrNull()?.message)
    }

    @Test
    fun _03_ShouldReturnFailure_WhenServerReturnsError() = runTest {
        coEvery { repository.createOrder() } returns
            Result.failure(RuntimeException("Cart is empty"))

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals("Cart is empty", result.exceptionOrNull()?.message)
    }
}
