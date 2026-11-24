package com.edgareldy.tutorial.cart.di

import com.edgareldy.tutorial.cart.data.api.CartApiService
import com.edgareldy.tutorial.cart.data.repository.CartRepositoryImpl
import com.edgareldy.tutorial.cart.domain.repository.CartRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Hilt module that wires cart dependencies.
 * CartApiService is provided via the shared Retrofit instance from :core:network.
 * CartRepository is bound to its implementation as a singleton.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CartModule {

    @Binds
    @Singleton
    abstract fun bindCartRepository(impl: CartRepositoryImpl): CartRepository

    companion object {

        @Provides
        @Singleton
        fun provideCartApiService(retrofit: Retrofit): CartApiService =
            retrofit.create(CartApiService::class.java)
    }
}
