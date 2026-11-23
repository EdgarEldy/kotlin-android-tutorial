package com.edgareldy.tutorial.domain.usecase.cart

import com.edgareldy.tutorial.domain.repository.CartRepository
import javax.inject.Inject

/**
 * Returns the total number of individual articles currently in the cart.
 * Used by the bottom navigation bar to display a badge count on the Cart icon.
 * Returns 0 on any error so the badge simply disappears rather than showing stale data.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class GetCartItemCountUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(): Int {
        return repository.getCart()
            .getOrNull()
            ?.items
            ?.sumOf { it.quantity }
            ?: 0
    }
}
