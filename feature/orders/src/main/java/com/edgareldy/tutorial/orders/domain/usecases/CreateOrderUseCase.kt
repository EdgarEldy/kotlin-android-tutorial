package com.edgareldy.tutorial.orders.domain.usecases

import com.edgareldy.tutorial.cart.domain.usecases.ClearCartUseCase
import com.edgareldy.tutorial.orders.domain.model.Order
import com.edgareldy.tutorial.orders.domain.repository.OrderRepository
import javax.inject.Inject

/**
 * Triggers a checkout by sending POST /api/v1/orders without a body.
 * The backend creates the order from the authenticated user's current cart.
 * On success, persists the new order locally and clears the local cart cache.
 * Cart clearing is business logic that belongs in the use case, not the repository.
 * This is a one-shot operation that requires network connectivity.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
class CreateOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val clearCartUseCase: ClearCartUseCase
) {
    suspend operator fun invoke(): Result<Order> =
        orderRepository.createOrder().onSuccess { clearCartUseCase() }
}
