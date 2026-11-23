package com.edgareldy.tutorial.domain.usecase.order

import com.edgareldy.tutorial.domain.model.Order
import com.edgareldy.tutorial.domain.repository.OrderRepository
import javax.inject.Inject

/**
 * Triggers the checkout flow by creating an order from the current cart.
 * This operation always requires a network connection; the backend rejects
 * the request if the cart is empty or the user is unauthenticated.
 * The resulting Order is upserted into the local cache by the repository.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class CreateOrderUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(): Result<Order> = repository.createOrder()
}
