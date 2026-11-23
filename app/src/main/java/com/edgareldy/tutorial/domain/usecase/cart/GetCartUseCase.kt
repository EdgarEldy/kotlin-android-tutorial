package com.edgareldy.tutorial.domain.usecase.cart

import com.edgareldy.tutorial.domain.model.Cart
import com.edgareldy.tutorial.domain.repository.CartRepository
import javax.inject.Inject

/**
 * Fetches the current user's cart from the backend.
 * Returns Empty-equivalent by emitting an empty cart when the response has no items,
 * but delegates that distinction to the ViewModel which maps it to the correct UiState.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class GetCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(): Result<Cart> = repository.getCart()
}
