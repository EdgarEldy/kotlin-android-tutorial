package com.edgareldy.tutorial.cart.data.repository

import com.edgareldy.tutorial.cart.data.api.CartApiService
import com.edgareldy.tutorial.cart.data.dto.AddCartItemRequest
import com.edgareldy.tutorial.cart.data.dto.CartItemResponse
import com.edgareldy.tutorial.cart.data.dto.CartResponse
import com.edgareldy.tutorial.cart.data.dto.UpdateCartItemRequest
import com.edgareldy.tutorial.cart.domain.model.Cart
import com.edgareldy.tutorial.cart.domain.model.CartItem
import com.edgareldy.tutorial.cart.domain.repository.CartRepository
import javax.inject.Inject

/**
 * Network-only implementation of [CartRepository].
 * Cart state is always authoritative on the backend, so no local cache is maintained.
 * Every operation wraps the API call in runCatching to convert exceptions into Result.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
class CartRepositoryImpl @Inject constructor(
    private val apiService: CartApiService
) : CartRepository {

    override suspend fun getCart(): Result<Cart> = runCatching {
        apiService.getCart().data!!.toDomain()
    }

    override suspend fun addItem(productId: Long, quantity: Int): Result<Cart> = runCatching {
        apiService.addItem(AddCartItemRequest(productId, quantity)).data!!.toDomain()
    }

    override suspend fun updateItem(itemId: Long, quantity: Int): Result<Cart> = runCatching {
        apiService.updateItem(itemId, UpdateCartItemRequest(quantity)).data!!.toDomain()
    }

    override suspend fun removeItem(itemId: Long): Result<Cart> = runCatching {
        apiService.removeItem(itemId).data!!.toDomain()
    }

    override suspend fun clearCart(): Result<Cart> = runCatching {
        apiService.clearCart().data!!.toDomain()
    }

    private fun CartResponse.toDomain(): Cart = Cart(
        id = id ?: 0L,
        items = items.map { it.toDomain() },
        total = total
    )

    private fun CartItemResponse.toDomain(): CartItem = CartItem(
        id = id,
        productId = productId,
        productName = productName,
        unitPrice = unitPrice,
        quantity = quantity,
        subtotal = subtotal
    )
}
