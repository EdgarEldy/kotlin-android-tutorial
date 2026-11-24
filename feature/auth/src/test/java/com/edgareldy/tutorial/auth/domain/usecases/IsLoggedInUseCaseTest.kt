package com.edgareldy.tutorial.auth.domain.usecases

import com.edgareldy.tutorial.auth.domain.repository.AuthRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for IsLoggedInUseCase.
 * Verifies that the use case delegates synchronously to AuthRepository.isLoggedIn()
 * and correctly reflects the stored token state without making a network call.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class IsLoggedInUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var isLoggedInUseCase: IsLoggedInUseCase

    @Before
    fun setUp() {
        authRepository = mockk()
        isLoggedInUseCase = IsLoggedInUseCase(authRepository)
    }

    @Test
    fun _01_ShouldReturnTrue_WhenValidTokenExists() {
        every { authRepository.isLoggedIn() } returns true

        val result = isLoggedInUseCase()

        assertTrue(result)
        verify(exactly = 1) { authRepository.isLoggedIn() }
    }

    @Test
    fun _02_ShouldReturnFalse_WhenNoTokenIsStored() {
        every { authRepository.isLoggedIn() } returns false

        val result = isLoggedInUseCase()

        assertFalse(result)
        verify(exactly = 1) { authRepository.isLoggedIn() }
    }

    @Test
    fun _03_ShouldNeverMakeNetworkCall_WhenCheckingLoginState() {
        every { authRepository.isLoggedIn() } returns false

        isLoggedInUseCase()

        // Only the synchronous isLoggedIn() should be called - no suspend functions
        verify(exactly = 1) { authRepository.isLoggedIn() }
    }
}
