package com.edgareldy.tutorial.orders.di

import com.edgareldy.tutorial.data.local.TutorialDatabase
import com.edgareldy.tutorial.data.local.dao.OrderDao
import com.edgareldy.tutorial.orders.data.api.OrderApiService
import com.edgareldy.tutorial.orders.data.repository.OrderRepositoryImpl
import com.edgareldy.tutorial.orders.domain.repository.OrderRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Hilt module that wires order dependencies.
 * OrderApiService is created from the shared authenticated Retrofit instance
 * provided by :core:network. OrderDao is obtained from TutorialDatabase which
 * is provided by :core:database. OrderRepository is bound to its implementation.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class OrdersModule {

    @Binds
    @Singleton
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository

    companion object {

        @Provides
        @Singleton
        fun provideOrderApiService(retrofit: Retrofit): OrderApiService =
            retrofit.create(OrderApiService::class.java)

        @Provides
        @Singleton
        fun provideOrderDao(database: TutorialDatabase): OrderDao =
            database.orderDao()
    }
}
