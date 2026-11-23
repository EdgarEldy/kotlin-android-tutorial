package com.edgareldy.tutorial.domain.usecase.order

import com.edgareldy.tutorial.domain.model.Order
import com.edgareldy.tutorial.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Returns a cache-first flow of the user's orders.
 * The repository emits the Room cache immediately and refreshes from
 * the network in the background, so the UI renders instantly even when
 * the device is offline.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class GetOrdersUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    operator fun invoke(page: Int = 0, size: Int = 20): Flow<Result<List<Order>>> =
        repository.getOrders(page, size)
}
