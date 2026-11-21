package com.edgareldy.tutorial.core.network

import com.edgareldy.tutorial.core.storage.SecureTokenStorage
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

/**
 * OkHttp Authenticator that handles 401 responses by attempting a token refresh.
 * On success it saves new tokens and retries the original request transparently.
 * On failure it clears all stored tokens, triggering a redirect to sign-in
 * on the next protected request.
 *
 * Note: the actual refresh HTTP call uses a dedicated OkHttpClient (no authenticator)
 * provided via NetworkModule to avoid a circular dependency with the main client.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
class TokenAuthenticator @Inject constructor(
    private val tokenStorage: SecureTokenStorage
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite retry loops: if the previous response was also a 401, give up
        if (response.priorResponse?.code == 401) return null

        val refreshToken = tokenStorage.getRefreshToken() ?: run {
            tokenStorage.clearTokens()
            return null
        }

        // TODO: Phase 2 - inject Lazy<AuthService> (auth-only OkHttpClient without
        // this authenticator) and perform a synchronous token refresh here.
        // On success: tokenStorage.saveTokens(newAccess, newRefresh)
        //             return request.newBuilder().header("Authorization", "Bearer $newAccess").build()
        // On failure: tokenStorage.clearTokens(); return null
        @Suppress("UNUSED_VARIABLE")
        val unusedRefreshToken = refreshToken // placeholder until Phase 2

        tokenStorage.clearTokens()
        return null
    }
}
