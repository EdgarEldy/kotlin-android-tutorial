package com.edgareldy.tutorial.products.domain.usecases

import com.edgareldy.tutorial.products.domain.model.Product
import com.edgareldy.tutorial.products.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Returns a Flow emitting the product with the given id from the cache,
 * refreshing from the network in the background if the row is not found.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class GetProductByIdUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(id: Long): Flow<Result<Product>> = repository.getProductById(id)
}
