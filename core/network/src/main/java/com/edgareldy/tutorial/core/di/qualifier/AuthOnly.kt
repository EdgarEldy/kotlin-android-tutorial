package com.edgareldy.tutorial.core.di.qualifier

import javax.inject.Qualifier

/**
 * Qualifier for a plain OkHttpClient that does NOT carry the TokenAuthenticator.
 * Used by TokenAuthenticator to perform the token refresh call without entering
 * an infinite retry loop on 401 responses.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthOnly
