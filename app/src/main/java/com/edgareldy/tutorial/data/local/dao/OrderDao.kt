package com.edgareldy.tutorial.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.edgareldy.tutorial.data.local.entity.OrderEntity
import com.edgareldy.tutorial.data.local.entity.OrderItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for the orders and order_items tables.
 * Uses @Upsert so that a network refresh idempotently inserts or updates
 * rows without first deleting the whole cache (which would flicker the UI).
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Dao
interface OrderDao {

    @Upsert
    suspend fun upsertAll(orders: List<OrderEntity>)

    @Upsert
    suspend fun upsertItems(items: List<OrderItemEntity>)

    @Query("SELECT * FROM orders ORDER BY cachedAt DESC")
    fun getOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id")
    fun getOrderById(id: Long): Flow<OrderEntity?>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItems(orderId: Long): Flow<List<OrderItemEntity>>

    @Query("DELETE FROM orders")
    suspend fun deleteAll()
}
