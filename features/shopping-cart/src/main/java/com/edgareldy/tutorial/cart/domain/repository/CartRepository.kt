package com.edgareldy.tutorial.cart.domain.repository

import com.edgareldy.tutorial.cart.domain.model.Cart

/**
 * Contract for all cart operations. Every method is a suspend function returning
 * Result<Cart> because the backend is the single source of truth for the cart -
 * there is no local cache for write operations.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
interface CartRepository {
    suspend fun getCart(): Result<Cart>
    suspend fun addItem(productId: Long, quantity: Int): Result<Cart>
    suspend fun updateItem(itemId: Long, quantity: Int): Result<Cart>
    suspend fun removeItem(itemId: Long): Result<Cart>
    suspend fun clearCart(): Result<Cart>
}
