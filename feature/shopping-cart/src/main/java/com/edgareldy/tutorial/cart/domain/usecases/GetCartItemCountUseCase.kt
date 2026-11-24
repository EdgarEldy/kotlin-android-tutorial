package com.edgareldy.tutorial.cart.domain.usecases

import com.edgareldy.tutorial.cart.domain.repository.CartRepository
import javax.inject.Inject

/**
 * Returns the total number of items in the cart, used for the badge on the bottom nav.
 * Returns 0 on any error so the UI degrades silently instead of showing an error state.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
class GetCartItemCountUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(): Int {
        return repository.getCart()
            .map { cart -> cart.items.sumOf { it.quantity } }
            .getOrElse { 0 }
    }
}
