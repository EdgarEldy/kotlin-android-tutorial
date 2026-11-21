package com.edgareldy.tutorial.features.auth.viewmodel

import com.edgareldy.tutorial.domain.model.User
import com.edgareldy.tutorial.domain.usecase.auth.GetCurrentUserUseCase
import com.edgareldy.tutorial.domain.usecase.auth.LogoutUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ProfileViewModel.
 * Verifies that the profile is loaded automatically on construction,
 * that UiState transitions (Loading, Success, Error, LoggedOut) are correct,
 * and that the logout action transitions state to LoggedOut.
 * The ViewModel is created inside each test so that mock answers are set up
 * before the init block triggers loadProfile().
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var logoutUseCase: LogoutUseCase

    private val fakeUser = User(
        id = 1L,
        firstName = "John",
        lastName = "Doe",
        username = "johndoe",
        email = "john@example.com",
        enabled = true
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getCurrentUserUseCase = mockk()
        logoutUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun _01_ShouldBeLoading_WhenProfileIsBeingFetched() = runTest(testDispatcher) {
        // delay forces a real suspension so the Loading state is observable before Success
        coEvery { getCurrentUserUseCase() } coAnswers {
            delay(1_000)
            Result.success(fakeUser)
        }

        val viewModel = ProfileViewModel(getCurrentUserUseCase, logoutUseCase)

        assertEquals(ProfileUiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()
        assertEquals(ProfileUiState.Success(fakeUser), viewModel.uiState.value)
    }

    @Test
    fun _02_ShouldUpdateStateToSuccess_WhenUserIsRetrieved() = runTest(testDispatcher) {
        coEvery { getCurrentUserUseCase() } returns Result.success(fakeUser)

        val viewModel = ProfileViewModel(getCurrentUserUseCase, logoutUseCase)
        advanceUntilIdle()

        assertEquals(ProfileUiState.Success(fakeUser), viewModel.uiState.value)
    }

    @Test
    fun _03_ShouldUpdateStateToError_WhenNetworkRequestFails() = runTest(testDispatcher) {
        val networkError = RuntimeException("Failed to load profile")
        coEvery { getCurrentUserUseCase() } returns Result.failure(networkError)

        val viewModel = ProfileViewModel(getCurrentUserUseCase, logoutUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Error)
        assertEquals("Failed to load profile", (state as ProfileUiState.Error).message)
    }

    @Test
    fun _04_ShouldUseFallbackMessage_WhenExceptionHasNoMessage() = runTest(testDispatcher) {
        coEvery { getCurrentUserUseCase() } returns Result.failure(RuntimeException())

        val viewModel = ProfileViewModel(getCurrentUserUseCase, logoutUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Error)
        assertEquals("Failed to load profile", (state as ProfileUiState.Error).message)
    }

    @Test
    fun _05_ShouldUpdateStateToLoggedOut_WhenLogoutSucceeds() = runTest(testDispatcher) {
        coEvery { getCurrentUserUseCase() } returns Result.success(fakeUser)
        coEvery { logoutUseCase() } returns Result.success(Unit)

        val viewModel = ProfileViewModel(getCurrentUserUseCase, logoutUseCase)
        advanceUntilIdle()

        viewModel.logout()
        advanceUntilIdle()

        assertEquals(ProfileUiState.LoggedOut, viewModel.uiState.value)
    }

    @Test
    fun _06_ShouldUpdateStateToLoggedOut_EvenWhenServerSideLogoutFails() = runTest(testDispatcher) {
        coEvery { getCurrentUserUseCase() } returns Result.success(fakeUser)
        coEvery { logoutUseCase() } returns Result.failure(RuntimeException("Server error"))

        val viewModel = ProfileViewModel(getCurrentUserUseCase, logoutUseCase)
        advanceUntilIdle()

        viewModel.logout()
        advanceUntilIdle()

        // The ViewModel always sets LoggedOut after calling logoutUseCase,
        // regardless of whether the use case succeeded or failed.
        assertEquals(ProfileUiState.LoggedOut, viewModel.uiState.value)
    }
}
