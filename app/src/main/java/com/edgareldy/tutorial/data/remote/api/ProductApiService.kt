package com.edgareldy.tutorial.data.remote.api

import com.edgareldy.tutorial.data.remote.dto.ApiResponse
import com.edgareldy.tutorial.data.remote.dto.PageResponse
import com.edgareldy.tutorial.data.remote.dto.ProductResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service for the /api/v1/products/ endpoints.
 * All queries are optional to support the product list screen's filters
 * (category, search, price range) independently or combined.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
interface ProductApiService {

    @GET("api/v1/products/")
    suspend fun getProducts(
        @Query("categoryId") categoryId: Long? = null,
        @Query("productName") productName: String? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResponse<ProductResponse>>

    @GET("api/v1/products/{id}")
    suspend fun getProductById(
        @Path("id") id: Long
    ): ApiResponse<ProductResponse>
}
