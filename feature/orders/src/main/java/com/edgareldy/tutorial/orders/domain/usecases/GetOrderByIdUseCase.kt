package com.edgareldy.tutorial.orders.domain.usecases

import com.edgareldy.tutorial.orders.domain.model.Order
import com.edgareldy.tutorial.orders.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Returns a single order identified by its server-assigned id as a Flow.
 * The repository emits from cache first, then refreshes from the network
 * in the background and emits again when fresh data is available.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
class GetOrderByIdUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    operator fun invoke(id: Long): Flow<Result<Order>> = orderRepository.getOrderById(id)
}
