package com.edgareldy.tutorial.auth.domain.usecase

import com.edgareldy.tutorial.auth.domain.model.User
import com.edgareldy.tutorial.auth.domain.repository.AuthRepository
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
