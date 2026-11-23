package com.edgareldy.tutorial.domain.usecase.order

import com.edgareldy.tutorial.domain.model.Order
import com.edgareldy.tutorial.domain.model.OrderStatus
import com.edgareldy.tutorial.domain.repository.OrderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CreateOrderUseCase.
 * Verifies that the use case delegates to the repository and correctly
 * propagates both success and failure results without any transformation.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class CreateOrderUseCaseTest {

    private lateinit var mockRepository: OrderRepository
    private lateinit var createOrderUseCase: CreateOrderUseCase

    // --- Helpers ---

    private fun fakeOrder(id: Long = 1L) = Order(
        id = id,
        orderNumber = "ORD-$id",
        status = OrderStatus.PENDING,
        totalAmount = 49.99,
        items = emptyList()
    )

    @Before
    fun setUp() {
        mockRepository = mockk()
        createOrderUseCase = CreateOrderUseCase(mockRepository)
    }

    // --- Tests ---

    @Test
    fun `invoke returns Result success when repository createOrder succeeds`() = runTest {
        // given
        val fakeOrder = fakeOrder(1L)
        coEvery { mockRepository.createOrder() } returns Result.success(fakeOrder)

        // when
        val result = createOrderUseCase()

        // then
        assertTrue(result.isSuccess)
        assertEquals(fakeOrder, result.getOrNull())
        coVerify(exactly = 1) { mockRepository.createOrder() }
    }

    @Test
    fun `invoke returns Result failure when repository createOrder fails with network error`() = runTest {
        // given
        coEvery { mockRepository.createOrder() } returns Result.failure(RuntimeException("Network error"))

        // when
        val result = createOrderUseCase()

        // then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { mockRepository.createOrder() }
    }
}
