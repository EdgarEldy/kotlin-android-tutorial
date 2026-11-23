package com.edgareldy.tutorial.domain.usecase.cart

import com.edgareldy.tutorial.domain.model.Cart
import com.edgareldy.tutorial.domain.repository.CartRepository
import javax.inject.Inject

/**
 * Removes all items from the cart in a single backend call.
 * The backend returns the now-empty cart so the ViewModel can transition
 * the screen to the Empty state without an extra network round-trip.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class ClearCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(): Result<Cart> = repository.clearCart()
}
