package com.edgareldy.tutorial.domain.usecase.order

import com.edgareldy.tutorial.domain.repository.OrderRepository
import javax.inject.Inject

/**
 * Forces an immediate network refresh of the orders cache.
 * Called when the user performs a pull-to-refresh gesture on the order
 * list screen, bypassing the automatic background refresh that fires
 * on first collection of GetOrdersUseCase.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class RefreshOrdersUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke() = repository.refreshOrders()
}
