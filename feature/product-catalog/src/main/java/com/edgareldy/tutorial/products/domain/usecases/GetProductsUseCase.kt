package com.edgareldy.tutorial.products.domain.usecases

import com.edgareldy.tutorial.products.domain.model.Product
import com.edgareldy.tutorial.products.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Returns a Flow of products, optionally filtered by category, search query
 * and price range. Filtering is applied here (domain layer) on the list
 * emitted by the repository. The repository handles only persistence concerns.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class GetProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(
        categoryId: Long? = null,
        search: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        page: Int = 0,
        size: Int = 20
    ): Flow<Result<List<Product>>> =
        repository.getProducts(categoryId, page, size)
            .map { result: Result<List<Product>> ->
                result.map { list: List<Product> ->
                    var filtered = list
                    if (!search.isNullOrBlank()) {
                        filtered = filtered.filter { p: Product -> p.name.contains(search, ignoreCase = true) }
                    }
                    if (minPrice != null) {
                        filtered = filtered.filter { p: Product -> p.unitPrice >= minPrice }
                    }
                    if (maxPrice != null) {
                        filtered = filtered.filter { p: Product -> p.unitPrice <= maxPrice }
                    }
                    filtered
                }
            }
}
