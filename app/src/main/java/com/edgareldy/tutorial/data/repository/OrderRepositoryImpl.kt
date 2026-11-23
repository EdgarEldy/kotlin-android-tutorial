package com.edgareldy.tutorial.data.repository

import com.edgareldy.tutorial.core.di.ApplicationScope
import com.edgareldy.tutorial.data.local.dao.OrderDao
import com.edgareldy.tutorial.data.local.entity.OrderEntity
import com.edgareldy.tutorial.data.local.entity.OrderItemEntity
import com.edgareldy.tutorial.data.remote.api.OrderApiService
import com.edgareldy.tutorial.data.remote.dto.OrderResponse
import com.edgareldy.tutorial.domain.model.Order
import com.edgareldy.tutorial.domain.model.OrderItem
import com.edgareldy.tutorial.domain.model.OrderStatus
import com.edgareldy.tutorial.domain.repository.OrderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache-first implementation of OrderRepository.
 * getOrders emits cached rows from Room immediately, then triggers a
 * background network refresh. getOrderById combines the order header flow
 * with its items flow so the detail screen is always consistent.
 * createOrder always requires the network and upserts the new order into
 * Room on success so it appears instantly in the order list.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val orderApiService: OrderApiService,
    private val orderDao: OrderDao,
    @ApplicationScope private val scope: CoroutineScope
) : OrderRepository {

    override fun getOrders(page: Int, size: Int): Flow<Result<List<Order>>> =
        orderDao.getOrders()
            // items loaded on demand by getOrderById - not needed for the list screen
            .map { entities -> Result.success(entities.map { it.toDomain(emptyList()) }) }
            .onStart {
                scope.launch {
                    runCatching { refreshOrders() }
                }
            }

    override fun getOrderById(id: Long): Flow<Result<Order>> =
        combine(
            orderDao.getOrderById(id),
            orderDao.getOrderItems(id)
        ) { entity, itemEntities ->
            if (entity != null) {
                Result.success(entity.toDomain(itemEntities.map { it.toDomain() }))
            } else {
                Result.failure(NoSuchElementException("Order $id not found in cache"))
            }
        }.onStart {
            scope.launch {
                runCatching {
                    val response = orderApiService.getOrderById(id)
                    val dto = response.data ?: return@runCatching
                    upsertOrderWithItems(dto)
                }
            }
        }

    override suspend fun createOrder(): Result<Order> =
        runCatching {
            val response = orderApiService.createOrder()
            val dto = response.data
                ?: throw IllegalStateException(
                    response.message ?: "Checkout failed: empty response"
                )
            upsertOrderWithItems(dto)
            dto.toDomain()
        }

    override suspend fun refreshOrders() {
        val response = orderApiService.getOrders(page = 0, size = 50)
        val page = response.data ?: return
        val orderEntities = page.content.map { it.toEntity() }
        val itemEntities = page.content.flatMap { dto ->
            dto.items.map { item ->
                OrderItemEntity(
                    id = item.id,
                    orderId = dto.id,
                    productId = item.productId,
                    productName = item.productName,
                    unitPrice = item.unitPrice,
                    quantity = item.quantity,
                    subtotal = item.subtotal
                )
            }
        }
        orderDao.deleteAll()
        orderDao.upsertAll(orderEntities)
        if (itemEntities.isNotEmpty()) {
            orderDao.upsertItems(itemEntities)
        }
    }

    // --- Private helpers ---

    private suspend fun upsertOrderWithItems(dto: OrderResponse) {
        orderDao.upsertAll(listOf(dto.toEntity()))
        // Delete existing items first to avoid orphaned rows when the server
        // removes an article from an order between two fetches.
        orderDao.deleteItemsByOrderId(dto.id)
        if (dto.items.isNotEmpty()) {
            val itemEntities = dto.items.map { item ->
                OrderItemEntity(
                    id = item.id,
                    orderId = dto.id,
                    productId = item.productId,
                    productName = item.productName,
                    unitPrice = item.unitPrice,
                    quantity = item.quantity,
                    subtotal = item.subtotal
                )
            }
            orderDao.upsertItems(itemEntities)
        }
    }

    // --- Mappers ---

    private fun OrderResponse.toEntity() = OrderEntity(
        id = id,
        orderNumber = orderNumber,
        status = status,
        totalAmount = totalAmount
    )

    private fun OrderResponse.toDomain() = Order(
        id = id,
        orderNumber = orderNumber,
        status = status.toOrderStatus(),
        totalAmount = totalAmount,
        items = items.map { item ->
            OrderItem(
                id = item.id,
                productId = item.productId,
                productName = item.productName,
                unitPrice = item.unitPrice,
                quantity = item.quantity,
                subtotal = item.subtotal
            )
        }
    )

    private fun OrderEntity.toDomain(items: List<OrderItem>) = Order(
        id = id,
        orderNumber = orderNumber,
        status = status.toOrderStatus(),
        totalAmount = totalAmount,
        items = items
    )

    private fun OrderItemEntity.toDomain() = OrderItem(
        id = id,
        productId = productId,
        productName = productName,
        unitPrice = unitPrice,
        quantity = quantity,
        subtotal = subtotal
    )

    private fun String.toOrderStatus(): OrderStatus = when (this.uppercase()) {
        "CONFIRMED" -> OrderStatus.CONFIRMED
        "CANCELLED" -> OrderStatus.CANCELLED
        else -> OrderStatus.PENDING
    }
}
