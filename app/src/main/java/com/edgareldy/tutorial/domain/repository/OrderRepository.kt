package com.edgareldy.tutorial.domain.repository

import com.edgareldy.tutorial.domain.model.Order
import kotlinx.coroutines.flow.Flow

/**
 * Contract for accessing and creating orders.
 * getOrders and getOrderById return Flow so the UI reacts automatically
 * when the Room cache is updated after a network refresh.
 * createOrder is a one-shot suspend function: it always requires the network
 * and returns a plain Result rather than a Flow.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
interface OrderRepository {
    fun getOrders(page: Int = 0, size: Int = 20): Flow<Result<List<Order>>>
    fun getOrderById(id: Long): Flow<Result<Order>>
    suspend fun createOrder(): Result<Order>
    suspend fun refreshOrders()
}
