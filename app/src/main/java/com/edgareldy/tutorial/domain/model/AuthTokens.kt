package com.edgareldy.tutorial.domain.model

/**
 * Domain model holding the JWT pair returned after a successful login or token refresh.
 * expiresIn is provided by the backend (seconds) and can be used for proactive refresh.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)
