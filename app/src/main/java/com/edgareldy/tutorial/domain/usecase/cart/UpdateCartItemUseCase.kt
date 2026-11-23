package com.edgareldy.tutorial.domain.usecase.cart

import com.edgareldy.tutorial.domain.model.Cart
import com.edgareldy.tutorial.domain.repository.CartRepository
import javax.inject.Inject

/**
 * Updates the quantity of an existing cart line item.
 * Validates that quantity is at least 1 before calling the repository.
 * To remove an item completely, use [RemoveCartItemUseCase] instead.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class UpdateCartItemUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(itemId: Long, quantity: Int): Result<Cart> {
        if (quantity < 1) {
            return Result.failure(IllegalArgumentException("Quantity must be at least 1"))
        }
        return repository.updateItem(itemId, quantity)
    }
}
