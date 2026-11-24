package com.edgareldy.tutorial.orders.domain.usecases

import com.edgareldy.tutorial.orders.domain.repository.OrderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for RefreshOrdersUseCase.
 * Verifies that invoke() delegates to OrderRepository.refreshOrders()
 * and returns the Result as-is in both success and failure scenarios.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
class RefreshOrdersUseCaseTest {

    private lateinit var repository: OrderRepository
    private lateinit var useCase: RefreshOrdersUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = RefreshOrdersUseCase(repository)
    }

    @Test
    fun _01_ShouldDelegateToRepository_WhenInvoked() = runTest {
        coEvery { repository.refreshOrders() } returns Result.success(Unit)

        useCase()

        coVerify(exactly = 1) { repository.refreshOrders() }
    }

    @Test
    fun _02_ShouldReturnSuccess_WhenRepositoryRefreshSucceeds() = runTest {
        coEvery { repository.refreshOrders() } returns Result.success(Unit)

        val result = useCase()

        assertTrue(result.isSuccess)
    }

    @Test
    fun _03_ShouldReturnFailure_WhenRepositoryRefreshFails() = runTest {
        coEvery { repository.refreshOrders() } returns
            Result.failure(RuntimeException("Refresh failed"))

        val result = useCase()

        assertTrue(result.isFailure)
    }
}
