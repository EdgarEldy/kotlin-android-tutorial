package com.edgareldy.tutorial.domain.repository

import com.edgareldy.tutorial.domain.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for product data.
 * All reads return Flow so the UI reacts automatically when the Room cache is
 * updated after a background network refresh. refreshProducts is a suspend
 * function because it is a one-shot network call with no ongoing emission.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
interface ProductRepository {

    fun getProducts(
        categoryId: Long? = null,
        page: Int = 0,
        size: Int = 20
    ): Flow<Result<List<Product>>>

    fun getProductById(id: Long): Flow<Result<Product>>

    suspend fun refreshProducts(categoryId: Long? = null)
}
