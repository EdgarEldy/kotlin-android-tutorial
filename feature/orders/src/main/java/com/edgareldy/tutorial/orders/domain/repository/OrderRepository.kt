package com.edgareldy.tutorial.orders.domain.repository

import com.edgareldy.tutorial.orders.domain.model.Order
import kotlinx.coroutines.flow.Flow

/**
 * Contract for all order data operations.
 * Read operations return Flow<Result<T>> to allow the repository to push
 * cache updates and network refreshes as separate emissions.
 * createOrder is a one-shot suspend function because checkout is a write
 * that requires network connectivity and has no offline fallback.
 * refreshOrders forces a full network sync and returns a one-shot Result.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
interface OrderRepository {
    fun getOrders(): Flow<Result<List<Order>>>
    fun getOrderById(id: Long): Flow<Result<Order>>
    suspend fun createOrder(): Result<Order>
    suspend fun refreshOrders(): Result<Unit>
}
