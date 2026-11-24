package com.edgareldy.tutorial.auth.domain.model

/**
 * Domain model representing an authenticated user.
 * Lives exclusively in the domain layer - no Retrofit or Room annotations.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
data class User(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val enabled: Boolean
)
