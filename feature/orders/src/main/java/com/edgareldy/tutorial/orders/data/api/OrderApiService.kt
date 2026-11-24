package com.edgareldy.tutorial.orders.data.api

import com.edgareldy.tutorial.orders.data.dto.ApiResponse
import com.edgareldy.tutorial.orders.data.dto.OrderResponse
import com.edgareldy.tutorial.orders.data.dto.PageResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service for the /api/v1/orders/ endpoints.
 * All operations require JWT authentication (attached by AuthInterceptor from :core:network).
 * createOrder() sends no request body - the backend creates the order from the user's cart.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
interface OrderApiService {

    @GET("api/v1/orders")
    suspend fun getOrders(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
        @Query("sort") sort: String = "id,desc"
    ): ApiResponse<PageResponse<OrderResponse>>

    @GET("api/v1/orders/{id}")
    suspend fun getOrderById(
        @Path("id") id: Long
    ): ApiResponse<OrderResponse>

    @POST("api/v1/orders")
    suspend fun createOrder(): ApiResponse<OrderResponse>
}
