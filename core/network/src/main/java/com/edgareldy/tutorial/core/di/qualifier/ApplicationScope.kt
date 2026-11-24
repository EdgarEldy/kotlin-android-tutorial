package com.edgareldy.tutorial.core.di.qualifier

import javax.inject.Qualifier

/**
 * Qualifier for the application-level CoroutineScope tied to the process lifetime.
 * Defined in core:network so any feature module can reference it without
 * creating an upward dependency on :app.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
