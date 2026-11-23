package com.edgareldy.tutorial.domain.usecase.order

import com.edgareldy.tutorial.domain.model.Order
import com.edgareldy.tutorial.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Returns a reactive stream for a single order by its id.
 * Uses the same cache-first strategy as GetOrdersUseCase: the detail
 * screen loads instantly from Room and updates when a network refresh
 * writes new data.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class GetOrderByIdUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    operator fun invoke(id: Long): Flow<Result<Order>> =
        repository.getOrderById(id)
}
