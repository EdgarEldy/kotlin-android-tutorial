package com.edgareldy.tutorial.cart.domain.usecases

import com.edgareldy.tutorial.cart.domain.model.Cart
import com.edgareldy.tutorial.cart.domain.repository.CartRepository
import javax.inject.Inject

/**
 * Removes all items from the cart in a single call.
 * Used after a successful checkout to reset the cart state.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
class ClearCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(): Result<Cart> = repository.clearCart()
}
