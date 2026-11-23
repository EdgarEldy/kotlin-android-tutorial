package com.edgareldy.tutorial.domain.usecase.auth

import com.edgareldy.tutorial.domain.model.User
import com.edgareldy.tutorial.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Fetches the profile of the currently authenticated user from the backend.
 * Delegates entirely to [AuthRepository.getMe] since there is no client-side logic needed.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<User> = authRepository.getMe()
}
