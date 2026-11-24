package com.edgareldy.tutorial.products.domain.usecases

import com.edgareldy.tutorial.products.domain.repository.ProductRepository
import javax.inject.Inject

/**
 * Triggers an explicit network refresh of the product cache.
 * Called on pull-to-refresh; the results are delivered via the Flow returned
 * by [GetProductsUseCase] rather than through this use case's return value.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class RefreshProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(categoryId: Long? = null) =
        repository.refreshProducts(categoryId)
}
