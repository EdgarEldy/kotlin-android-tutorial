package com.edgareldy.tutorial.orders.domain.usecases

import com.edgareldy.tutorial.orders.domain.model.Order
import com.edgareldy.tutorial.orders.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Returns the full order list as a Flow. The first emission comes from the
 * local Room cache (possibly empty), and subsequent emissions arrive after
 * the repository completes its background network refresh.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
class GetOrdersUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    operator fun invoke(): Flow<Result<List<Order>>> = orderRepository.getOrders()
}
