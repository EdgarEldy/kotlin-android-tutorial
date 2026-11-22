package com.edgareldy.tutorial.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifier to inject Dispatchers.IO for I/O-bound coroutines (network, disk).
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class IoDispatcher

/**
 * Qualifier to inject Dispatchers.Main for UI-bound coroutines.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class MainDispatcher

/**
 * Qualifier to inject Dispatchers.Default for CPU-bound coroutines.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class DefaultDispatcher

/**
 * Qualifier to inject an application-level CoroutineScope tied to the process lifetime.
 * Used by repositories that need to launch background refresh jobs that survive
 * individual ViewModel cancellations.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class ApplicationScope

/**
 * Hilt module that provides named CoroutineDispatchers and an application-level scope
 * for injection. Using qualifiers instead of hardcoding Dispatchers.IO in repositories
 * makes coroutine-based code testable by allowing dispatcher substitution in tests.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {
    @Provides @IoDispatcher fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    @Provides @MainDispatcher fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
    @Provides @DefaultDispatcher fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(@IoDispatcher ioDispatcher: CoroutineDispatcher): CoroutineScope =
        CoroutineScope(SupervisorJob() + ioDispatcher)
}
