package com.edgareldy.tutorial.auth.domain.usecases

import com.edgareldy.tutorial.auth.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Signs out the current user: invalidates the session server-side then clears local tokens.
 * Token cleanup is handled inside [AuthRepository.logout] to keep it atomic.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> = authRepository.logout()
}
