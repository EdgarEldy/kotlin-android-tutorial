package com.edgareldy.tutorial.core.network

import com.edgareldy.tutorial.core.di.qualifier.AuthOnly
import com.edgareldy.tutorial.core.storage.SecureTokenStorage
import com.edgareldy.tutorial.data.remote.dto.ApiResponse
import com.edgareldy.tutorial.data.remote.dto.AuthResponseDto
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

/**
 * OkHttp Authenticator that handles 401 responses by attempting a synchronous token refresh.
 * Uses the @AuthOnly OkHttpClient (no authenticator attached) to avoid an infinite retry loop.
 * On success, saves new tokens and retries the original request transparently.
 * On failure, clears all stored tokens so the next protected call gets a fresh 401
 * which the navigation layer will catch to redirect to sign-in.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
class TokenAuthenticator @Inject constructor(
    private val tokenStorage: SecureTokenStorage,
    @AuthOnly private val authOnlyOkHttpClient: OkHttpClient
) : Authenticator {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite retry: if the previous attempt was also 401, give up
        if (response.priorResponse?.code == 401) {
            tokenStorage.clearTokens()
            return null
        }

        val refreshToken = tokenStorage.getRefreshToken() ?: run {
            tokenStorage.clearTokens()
            return null
        }

        return try {
            val refreshRequest = Request.Builder()
                .url("${getBaseUrl(response)}api/v1/auth/refresh-token")
                .addHeader("Authorization", "Bearer $refreshToken")
                .post(ByteArray(0).toRequestBody(null))
                .build()

            val refreshResponse = authOnlyOkHttpClient.newCall(refreshRequest).execute()
            if (!refreshResponse.isSuccessful) {
                tokenStorage.clearTokens()
                return null
            }

            val body = refreshResponse.body?.string() ?: run {
                tokenStorage.clearTokens()
                return null
            }

            val apiResponse = json.decodeFromString<ApiResponse<AuthResponseDto>>(body)
            val authData = apiResponse.data ?: run {
                tokenStorage.clearTokens()
                return null
            }

            tokenStorage.saveTokens(authData.accessToken, authData.refreshToken)

            response.request.newBuilder()
                .removeHeader("Authorization")
                .addHeader("Authorization", "Bearer ${authData.accessToken}")
                .build()
        } catch (e: Exception) {
            tokenStorage.clearTokens()
            null
        }
    }

    private fun getBaseUrl(response: Response): String {
        val url = response.request.url
        return "${url.scheme}://${url.host}:${url.port}/"
    }
}
