package com.edgareldy.tutorial.core.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for secure storage bindings.
 * SecureTokenStorage is @Singleton and @Inject-annotated, so Hilt
 * resolves it automatically without an explicit @Provides method.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@Module
@InstallIn(SingletonComponent::class)
object SecureStorageModule
