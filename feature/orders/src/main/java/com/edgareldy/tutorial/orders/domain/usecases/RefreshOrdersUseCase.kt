package com.edgareldy.tutorial.orders.domain.usecases

import com.edgareldy.tutorial.orders.domain.repository.OrderRepository
import javax.inject.Inject

/**
 * Forces a full network sync of the orders list and updates the Room cache.
 * Intended to be called on pull-to-refresh or whenever the ViewModel
 * needs to bypass the cache-first strategy and fetch fresh data immediately.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
class RefreshOrdersUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(): Result<Unit> = orderRepository.refreshOrders()
}
