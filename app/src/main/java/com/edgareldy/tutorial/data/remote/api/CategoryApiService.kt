package com.edgareldy.tutorial.data.remote.api

import com.edgareldy.tutorial.data.remote.dto.ApiResponse
import com.edgareldy.tutorial.data.remote.dto.CategoryResponse
import com.edgareldy.tutorial.data.remote.dto.PageResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service for the /api/v1/categories/ endpoints.
 * getCategoryById is included to satisfy the spec but is not used in the
 * current phase since only the list is needed for the filter chip row.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
interface CategoryApiService {

    @GET("api/v1/categories/")
    suspend fun getCategories(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): ApiResponse<PageResponse<CategoryResponse>>

    @GET("api/v1/categories/{id}")
    suspend fun getCategoryById(
        @Path("id") id: Long
    ): ApiResponse<CategoryResponse>
}
