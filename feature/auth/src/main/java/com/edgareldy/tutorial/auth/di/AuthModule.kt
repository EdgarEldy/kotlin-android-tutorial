package com.edgareldy.tutorial.auth.di

import com.edgareldy.tutorial.auth.data.repository.AuthRepositoryImpl
import com.edgareldy.tutorial.auth.data.repository.PasswordResetRepositoryImpl
import com.edgareldy.tutorial.auth.domain.repository.AuthRepository
import com.edgareldy.tutorial.auth.domain.repository.PasswordResetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds repository interfaces to their implementations for the auth feature.
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
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPasswordResetRepository(impl: PasswordResetRepositoryImpl): PasswordResetRepository
}
