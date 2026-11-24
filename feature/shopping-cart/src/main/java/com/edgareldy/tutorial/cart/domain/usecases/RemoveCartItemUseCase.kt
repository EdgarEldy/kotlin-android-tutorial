package com.edgareldy.tutorial.cart.domain.usecases

import com.edgareldy.tutorial.cart.domain.model.Cart
import com.edgareldy.tutorial.cart.domain.repository.CartRepository
import javax.inject.Inject

/**
 * Removes a single item from the cart by its item id.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
class RemoveCartItemUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(itemId: Long): Result<Cart> = repository.removeItem(itemId)
}
