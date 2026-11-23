package com.edgareldy.tutorial.domain.usecase.cart

import com.edgareldy.tutorial.domain.model.Cart
import com.edgareldy.tutorial.domain.repository.CartRepository
import javax.inject.Inject

/**
 * Removes a single line item from the cart by its item id.
 * The backend returns the updated cart so the UI can refresh in one round-trip.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class RemoveCartItemUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(itemId: Long): Result<Cart> = repository.removeItem(itemId)
}
