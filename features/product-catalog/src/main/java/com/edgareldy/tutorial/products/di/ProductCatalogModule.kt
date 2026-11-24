package com.edgareldy.tutorial.products.di

import com.edgareldy.tutorial.products.data.api.CategoryApiService
import com.edgareldy.tutorial.products.data.api.ProductApiService
import com.edgareldy.tutorial.products.data.repository.CategoryRepositoryImpl
import com.edgareldy.tutorial.products.data.repository.ProductRepositoryImpl
import com.edgareldy.tutorial.products.domain.repository.CategoryRepository
import com.edgareldy.tutorial.products.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Hilt module for the product-catalog feature.
 * Binds repository interfaces to their implementations and provides the
 * Retrofit API service instances for products and categories.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ProductCatalogModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    companion object {
        @Provides
        @Singleton
        fun provideProductApiService(retrofit: Retrofit): ProductApiService =
            retrofit.create(ProductApiService::class.java)

        @Provides
        @Singleton
        fun provideCategoryApiService(retrofit: Retrofit): CategoryApiService =
            retrofit.create(CategoryApiService::class.java)
    }
}
