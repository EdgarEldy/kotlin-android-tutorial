package com.edgareldy.tutorial.auth.presentation.viewmodel

import app.cash.turbine.test
import com.edgareldy.tutorial.auth.domain.model.User
import com.edgareldy.tutorial.auth.domain.usecases.GetCurrentUserUseCase
import com.edgareldy.tutorial.auth.domain.usecases.LogoutUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
 * Verifies state transitions (Loading -> Success/Error, Success -> LoggedOut) using
 * Turbine to assert each emission in order. The ViewModel is instantiated inside each
 * test so that mocks are configured before the init block triggers loadProfile().
 * When a delay is added to the mock, the init coroutine suspends at that point,
 * leaving the state at Loading when Turbine attaches its collector.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
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
    fun _01_ShouldEmitLoadingThenSuccess_WhenProfileIsBeingFetched() = runTest(testDispatcher) {
        // The delay causes the init coroutine to suspend, leaving uiState at Loading
        // when Turbine attaches. Without a delay, UnconfinedTestDispatcher would run
        // the coroutine to completion before Turbine collects the first item.
        coEvery { getCurrentUserUseCase() } coAnswers {
            delay(100)
            Result.success(fakeUser)
        }

        val viewModel = ProfileViewModel(getCurrentUserUseCase, logoutUseCase)

        viewModel.uiState.test {
            assertEquals(ProfileUiState.Loading, awaitItem())
            assertEquals(ProfileUiState.Success(fakeUser), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _02_ShouldEmitSuccess_WhenUserIsRetrieved() = runTest(testDispatcher) {
        coEvery { getCurrentUserUseCase() } coAnswers { delay(1); Result.success(fakeUser) }

        val viewModel = ProfileViewModel(getCurrentUserUseCase, logoutUseCase)

        viewModel.uiState.test {
            skipItems(1) // consume Loading from init
            assertEquals(ProfileUiState.Success(fakeUser), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _03_ShouldEmitError_WhenNetworkRequestFails() = runTest(testDispatcher) {
        coEvery { getCurrentUserUseCase() } coAnswers { delay(1); Result.failure(RuntimeException("Failed to load profile")) }

        val viewModel = ProfileViewModel(getCurrentUserUseCase, logoutUseCase)

        viewModel.uiState.test {
            skipItems(1) // consume Loading from init
            val error = awaitItem()
            assertTrue(error is ProfileUiState.Error)
            assertEquals("Failed to load profile", (error as ProfileUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _04_ShouldUseFallbackMessage_WhenExceptionHasNoMessage() = runTest(testDispatcher) {
        coEvery { getCurrentUserUseCase() } coAnswers { delay(1); Result.failure(RuntimeException()) }

        val viewModel = ProfileViewModel(getCurrentUserUseCase, logoutUseCase)

        viewModel.uiState.test {
            skipItems(1) // consume Loading from init
            val error = awaitItem()
            assertTrue(error is ProfileUiState.Error)
            assertEquals("Failed to load profile", (error as ProfileUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _05_ShouldEmitLoggedOut_WhenLogoutIsCalled() = runTest(testDispatcher) {
        coEvery { getCurrentUserUseCase() } coAnswers { delay(1); Result.success(fakeUser) }
        coEvery { logoutUseCase() } coAnswers { delay(1); Result.success(Unit) }

        val viewModel = ProfileViewModel(getCurrentUserUseCase, logoutUseCase)

        viewModel.uiState.test {
            skipItems(1) // consume Loading from init
            assertEquals(ProfileUiState.Success(fakeUser), awaitItem())
            viewModel.logout()
            assertEquals(ProfileUiState.LoggedOut, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _06_ShouldEmitLoggedOut_EvenWhenServerSideLogoutFails() = runTest(testDispatcher) {
        coEvery { getCurrentUserUseCase() } coAnswers { delay(1); Result.success(fakeUser) }
        coEvery { logoutUseCase() } coAnswers { delay(1); Result.failure(RuntimeException("Server error")) }

        val viewModel = ProfileViewModel(getCurrentUserUseCase, logoutUseCase)

        viewModel.uiState.test {
            skipItems(1) // consume Loading from init
            assertEquals(ProfileUiState.Success(fakeUser), awaitItem())
            viewModel.logout()
            // The ViewModel always sets LoggedOut after calling logoutUseCase,
            // regardless of whether the use case succeeded or failed.
            assertEquals(ProfileUiState.LoggedOut, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun _07_ShouldEmitLoadingThenSuccess_WhenProfileIsReloaded() = runTest(testDispatcher) {
        coEvery { getCurrentUserUseCase() } coAnswers { delay(1); Result.success(fakeUser) }

        val viewModel = ProfileViewModel(getCurrentUserUseCase, logoutUseCase)

        viewModel.uiState.test {
            skipItems(1) // consume Loading from init
            assertEquals(ProfileUiState.Success(fakeUser), awaitItem()) // initial load
            coEvery { getCurrentUserUseCase() } coAnswers {
                delay(100)
                Result.success(fakeUser)
            }
            viewModel.loadProfile()
            assertEquals(ProfileUiState.Loading, awaitItem())
            assertEquals(ProfileUiState.Success(fakeUser), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
