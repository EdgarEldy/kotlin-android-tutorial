package com.edgareldy.tutorial.data.remote.api

import com.edgareldy.tutorial.data.remote.dto.ApiResponse
import com.edgareldy.tutorial.data.remote.dto.OrderResponse
import com.edgareldy.tutorial.data.remote.dto.PageResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface for the orders API endpoints.
 * createOrder takes no request body: the backend builds the order
 * directly from the authenticated user's current cart.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
interface OrderApiService {

    @GET("api/v1/orders")
    suspend fun getOrders(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ApiResponse<PageResponse<OrderResponse>>

    @GET("api/v1/orders/{id}")
    suspend fun getOrderById(
        @Path("id") id: Long
    ): ApiResponse<OrderResponse>

    @POST("api/v1/orders")
    suspend fun createOrder(): ApiResponse<OrderResponse>
}
