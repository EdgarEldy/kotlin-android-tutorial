package com.edgareldy.tutorial.core.di

import com.edgareldy.tutorial.data.repository.AuthRepositoryImpl
import com.edgareldy.tutorial.data.repository.CategoryRepositoryImpl
import com.edgareldy.tutorial.data.repository.PasswordResetRepositoryImpl
import com.edgareldy.tutorial.data.repository.ProductRepositoryImpl
import com.edgareldy.tutorial.domain.repository.AuthRepository
import com.edgareldy.tutorial.domain.repository.CategoryRepository
import com.edgareldy.tutorial.domain.repository.PasswordResetRepository
import com.edgareldy.tutorial.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds repository interfaces to their implementations.
 * Using @Binds instead of @Provides avoids generating an unnecessary wrapper class
 * and signals clearly that this is a pure interface-to-implementation mapping.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPasswordResetRepository(impl: PasswordResetRepositoryImpl): PasswordResetRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository
}
