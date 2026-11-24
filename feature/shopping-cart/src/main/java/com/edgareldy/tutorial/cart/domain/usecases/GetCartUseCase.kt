package com.edgareldy.tutorial.cart.domain.usecases

import com.edgareldy.tutorial.cart.domain.model.Cart
import com.edgareldy.tutorial.cart.domain.repository.CartRepository
import javax.inject.Inject

/**
 * Fetches the current user's cart from the backend.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
class GetCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(): Result<Cart> = repository.getCart()
}
