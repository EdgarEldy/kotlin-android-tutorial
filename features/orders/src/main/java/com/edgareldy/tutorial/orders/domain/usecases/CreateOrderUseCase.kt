package com.edgareldy.tutorial.orders.domain.usecases

import com.edgareldy.tutorial.orders.domain.model.Order
import com.edgareldy.tutorial.orders.domain.repository.OrderRepository
import javax.inject.Inject

/**
 * Triggers a checkout by sending POST /api/v1/orders without a body.
 * The backend creates the order from the authenticated user's current cart.
 * On success the repository clears the cart and persists the new order locally.
 * This is a one-shot operation that requires network connectivity.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
class CreateOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(): Result<Order> = orderRepository.createOrder()
}
