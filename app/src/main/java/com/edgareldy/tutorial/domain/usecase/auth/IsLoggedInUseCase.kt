package com.edgareldy.tutorial.domain.usecase.auth

import com.edgareldy.tutorial.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Synchronously checks whether a valid access token exists in local storage.
 * Used at app startup to decide whether to route to the auth graph or the main graph.
 * No network call is made.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
class IsLoggedInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Boolean = authRepository.isLoggedIn()
}
