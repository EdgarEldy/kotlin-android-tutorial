package com.edgareldy.tutorial.cart.domain.usecases

import com.edgareldy.tutorial.cart.domain.model.Cart
import com.edgareldy.tutorial.cart.domain.repository.CartRepository
import javax.inject.Inject

/**
 * Adds a product to the cart with the given quantity.
 * Validates that quantity is at least 1 before calling the repository.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
class AddCartItemUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(productId: Long, quantity: Int): Result<Cart> {
        if (quantity < 1) {
            return Result.failure(IllegalArgumentException("Quantity must be at least 1"))
        }
        return repository.addItem(productId, quantity)
    }
}
