package com.edgareldy.tutorial.data.repository

import com.edgareldy.tutorial.data.remote.api.CartApiService
import com.edgareldy.tutorial.data.remote.dto.AddCartItemRequest
import com.edgareldy.tutorial.data.remote.dto.CartResponse
import com.edgareldy.tutorial.data.remote.dto.UpdateCartItemRequest
import com.edgareldy.tutorial.domain.model.Cart
import com.edgareldy.tutorial.domain.model.CartItem
import com.edgareldy.tutorial.domain.repository.CartRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Network-only implementation of [CartRepository].
 * The cart has no local cache because write operations (add, update, remove)
 * must be consistent with the server state. Any network failure wraps the
 * IOException so the ViewModel can surface a human-readable error message.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Singleton
class CartRepositoryImpl @Inject constructor(
    private val cartApiService: CartApiService
) : CartRepository {

    override suspend fun getCart(): Result<Cart> = runCatching {
        val response = cartApiService.getCart()
        response.data?.toDomain()
            ?: throw IOException("Cart data is null")
    }

    override suspend fun addItem(productId: Long, quantity: Int): Result<Cart> = runCatching {
        val response = cartApiService.addItem(AddCartItemRequest(productId, quantity))
        response.data?.toDomain()
            ?: throw IOException("Cart data is null after adding item")
    }

    override suspend fun updateItem(itemId: Long, quantity: Int): Result<Cart> = runCatching {
        val response = cartApiService.updateItem(itemId, UpdateCartItemRequest(quantity))
        response.data?.toDomain()
            ?: throw IOException("Cart data is null after updating item")
    }

    override suspend fun removeItem(itemId: Long): Result<Cart> = runCatching {
        val response = cartApiService.removeItem(itemId)
        response.data?.toDomain()
            ?: throw IOException("Cart data is null after removing item")
    }

    override suspend fun clearCart(): Result<Cart> = runCatching {
        val response = cartApiService.clearCart()
        response.data?.toDomain()
            ?: throw IOException("Cart data is null after clearing cart")
    }

    // --- Mappers ---

    private fun CartResponse.toDomain() = Cart(
        id = id ?: 0L,
        items = items.map { item ->
            CartItem(
                id = item.id,
                productId = item.productId,
                productName = item.productName,
                unitPrice = item.unitPrice,
                quantity = item.quantity,
                subtotal = item.subtotal
            )
        },
        total = total
    )
}
