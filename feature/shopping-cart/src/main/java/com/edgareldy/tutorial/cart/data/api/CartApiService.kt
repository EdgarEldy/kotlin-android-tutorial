package com.edgareldy.tutorial.cart.data.api

import com.edgareldy.tutorial.cart.data.dto.AddCartItemRequest
import com.edgareldy.tutorial.cart.data.dto.ApiResponse
import com.edgareldy.tutorial.cart.data.dto.CartResponse
import com.edgareldy.tutorial.cart.data.dto.UpdateCartItemRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit service for the /api/v1/cart/ endpoints.
 * All operations require authentication (JWT Bearer token attached by AuthInterceptor).
 * Every mutating call returns the updated CartResponse so the UI stays in sync
 * without issuing a second GET after each write.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
interface CartApiService {

    @GET("api/v1/cart")
    suspend fun getCart(): ApiResponse<CartResponse>

    @POST("api/v1/cart/items")
    suspend fun addItem(
        @Body request: AddCartItemRequest
    ): ApiResponse<CartResponse>

    @PATCH("api/v1/cart/items/{itemId}")
    suspend fun updateItem(
        @Path("itemId") itemId: Long,
        @Body request: UpdateCartItemRequest
    ): ApiResponse<CartResponse>

    @DELETE("api/v1/cart/items/{itemId}")
    suspend fun removeItem(
        @Path("itemId") itemId: Long
    ): ApiResponse<CartResponse>

    @DELETE("api/v1/cart/items")
    suspend fun clearCart(): ApiResponse<CartResponse>
}
