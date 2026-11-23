package com.edgareldy.tutorial.core.di

import com.edgareldy.tutorial.BuildConfig
import com.edgareldy.tutorial.core.di.qualifier.AuthOnly
import com.edgareldy.tutorial.core.network.AuthInterceptor
import com.edgareldy.tutorial.core.network.TokenAuthenticator
import com.edgareldy.tutorial.data.remote.api.AuthApiService
import com.edgareldy.tutorial.data.remote.api.CartApiService
import com.edgareldy.tutorial.data.remote.api.CategoryApiService
import com.edgareldy.tutorial.data.remote.api.OrderApiService
import com.edgareldy.tutorial.data.remote.api.PasswordResetApiService
import com.edgareldy.tutorial.data.remote.api.ProductApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module that wires the entire network stack: OkHttp, Retrofit, logging,
 * auth interceptor, and token refresh authenticator.
 * BASE_URL is read from BuildConfig and varies per product flavor (dev/staging/prod).
 * A secondary @AuthOnly OkHttpClient without the TokenAuthenticator is provided
 * for use by TokenAuthenticator itself to avoid a circular dependency.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Plain OkHttpClient used only for the token refresh call inside TokenAuthenticator.
     * It carries the AuthInterceptor (attaches the access token) but NOT the
     * TokenAuthenticator, breaking the dependency cycle.
     */
    @Provides
    @Singleton
    @AuthOnly
    fun provideAuthOnlyOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                            else HttpLoggingInterceptor.Level.NONE
                }
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                        else HttpLoggingInterceptor.Level.NONE
            }
        )
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun providePasswordResetApiService(retrofit: Retrofit): PasswordResetApiService =
        retrofit.create(PasswordResetApiService::class.java)

    @Provides
    @Singleton
    fun provideProductApiService(retrofit: Retrofit): ProductApiService =
        retrofit.create(ProductApiService::class.java)

    @Provides
    @Singleton
    fun provideCategoryApiService(retrofit: Retrofit): CategoryApiService =
        retrofit.create(CategoryApiService::class.java)

    @Provides
    @Singleton
    fun provideCartApiService(retrofit: Retrofit): CartApiService =
        retrofit.create(CartApiService::class.java)

    @Provides
    @Singleton
    fun provideOrderApiService(retrofit: Retrofit): OrderApiService =
        retrofit.create(OrderApiService::class.java)
}
