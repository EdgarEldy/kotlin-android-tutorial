package com.edgareldy.tutorial.orders.data.repository

import com.edgareldy.tutorial.core.di.qualifier.ApplicationScope
import com.edgareldy.tutorial.data.local.dao.OrderDao
import com.edgareldy.tutorial.data.local.entity.OrderEntity
import com.edgareldy.tutorial.data.local.entity.OrderItemEntity
import com.edgareldy.tutorial.orders.data.api.OrderApiService
import com.edgareldy.tutorial.orders.data.dto.OrderItemResponse
import com.edgareldy.tutorial.orders.data.dto.OrderResponse
import com.edgareldy.tutorial.orders.domain.model.Order
import com.edgareldy.tutorial.orders.domain.model.OrderItem
import com.edgareldy.tutorial.orders.domain.model.OrderStatus
import com.edgareldy.tutorial.orders.domain.repository.OrderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Cache-first implementation of [OrderRepository].
 * Read operations emit from Room immediately, then refresh from the network
 * in a background coroutine and re-emit. Write operations (createOrder) require
 * the network; cart clearing after checkout is handled by CreateOrderUseCase.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao,
    private val apiService: OrderApiService,
    @ApplicationScope private val backgroundScope: CoroutineScope
) : OrderRepository {

    override fun getOrders(): Flow<Result<List<Order>>> = flow {
        // Collect orders and their items together from the cache
        orderDao.getOrders().collect { entities ->
            if (entities.isEmpty()) {
                // Try network immediately when cache is empty
                val networkResult = runCatching {
                    val response = apiService.getOrders()
                    response.data?.content ?: emptyList()
                }
                networkResult.fold(
                    onSuccess = { orderResponses ->
                        syncToCache(orderResponses)
                        emit(Result.success(orderResponses.map { it.toDomainWithItems() }))
                    },
                    onFailure = { error ->
                        emit(Result.failure(error))
                    }
                )
            } else {
                // Emit cached data immediately
                // items are not loaded for the list view to avoid N+1 queries; use getOrderById() for full detail
                val domainOrders = entities.map { entity ->
                    Order(
                        id = entity.id,
                        orderNumber = entity.orderNumber,
                        status = OrderStatus.valueOf(
                            runCatching { entity.status.uppercase() }
                                .getOrElse { "UNKNOWN" }
                                .let { if (OrderStatus.entries.any { s -> s.name == it }) it else "UNKNOWN" }
                        ),
                        totalAmount = entity.totalAmount,
                        items = emptyList()
                    )
                }
                emit(Result.success(domainOrders))

                // Background refresh
                backgroundScope.launch {
                    runCatching {
                        val response = apiService.getOrders()
                        response.data?.content ?: emptyList()
                    }.onSuccess { orderResponses ->
                        syncToCache(orderResponses)
                    }
                }
            }
        }
    }

    override fun getOrderById(id: Long): Flow<Result<Order>> =
        combine(
            orderDao.getOrderById(id),
            orderDao.getOrderItems(id)
        ) { entity, items ->
            if (entity == null) {
                runCatching {
                    val response = apiService.getOrderById(id)
                    val dto = response.data ?: error("Order not found")
                    val order = dto.toDomainWithItems()
                    // Persist to cache
                    orderDao.upsertAll(listOf(dto.toEntity()))
                    orderDao.deleteItemsByOrderId(id)
                    orderDao.upsertItems(dto.items.map { it.toEntity(id) })
                    order
                }
            } else {
                Result.success(
                    Order(
                        id = entity.id,
                        orderNumber = entity.orderNumber,
                        status = entity.status.toOrderStatus(),
                        totalAmount = entity.totalAmount,
                        items = items.map { it.toDomain() }
                    )
                )
            }
        }

    override suspend fun createOrder(): Result<Order> = runCatching {
        val response = apiService.createOrder()
        val dto = response.data ?: error("No order data returned")
        val order = dto.toDomainWithItems()
        // Persist new order to cache
        orderDao.upsertAll(listOf(dto.toEntity()))
        orderDao.deleteItemsByOrderId(dto.id)
        orderDao.upsertItems(dto.items.map { it.toEntity(dto.id) })
        order
    }

    override suspend fun refreshOrders(): Result<Unit> = runCatching {
        val response = apiService.getOrders()
        val orderResponses = response.data?.content ?: emptyList()
        syncToCache(orderResponses)
    }

    private suspend fun syncToCache(orderResponses: List<OrderResponse>) {
        orderDao.deleteAll()
        orderDao.upsertAll(orderResponses.map { it.toEntity() })
        orderResponses.forEach { dto ->
            orderDao.upsertItems(dto.items.map { it.toEntity(dto.id) })
        }
    }

    // Mapping extensions

    private fun OrderResponse.toEntity() = OrderEntity(
        id = id,
        orderNumber = orderNumber,
        status = status,
        totalAmount = totalAmount
    )

    private fun OrderItemResponse.toEntity(orderId: Long) = OrderItemEntity(
        id = id,
        orderId = orderId,
        productId = productId,
        productName = productName,
        unitPrice = unitPrice,
        quantity = quantity,
        subtotal = subtotal
    )

    private fun OrderResponse.toDomainWithItems() = Order(
        id = id,
        orderNumber = orderNumber,
        status = status.toOrderStatus(),
        totalAmount = totalAmount,
        items = items.map { it.toDomain() }
    )

    private fun OrderItemResponse.toDomain() = OrderItem(
        id = id,
        productId = productId,
        productName = productName,
        unitPrice = unitPrice,
        quantity = quantity,
        subtotal = subtotal
    )

    private fun OrderItemEntity.toDomain() = OrderItem(
        id = id,
        productId = productId,
        productName = productName,
        unitPrice = unitPrice,
        quantity = quantity,
        subtotal = subtotal
    )

    private fun String.toOrderStatus(): OrderStatus =
        runCatching { OrderStatus.valueOf(uppercase()) }.getOrDefault(OrderStatus.UNKNOWN)
}
