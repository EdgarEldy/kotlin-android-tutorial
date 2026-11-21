package com.edgareldy.tutorial.core.network

import com.edgareldy.tutorial.core.storage.SecureTokenStorage
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * OkHttp interceptor that attaches the JWT Bearer token to every outgoing request.
 * Auth endpoints (/auth/) are excluded to avoid circular calls during login/refresh.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
class AuthInterceptor @Inject constructor(
    private val tokenStorage: SecureTokenStorage
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = tokenStorage.getAccessToken()
        return if (token != null && !request.url.encodedPath.contains("/auth/")) {
            chain.proceed(
                request.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            )
        } else {
            chain.proceed(request)
        }
    }
}
