package com.edgareldy.tutorial.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgareldy.tutorial.auth.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the registration screen.
 * Success carries the email so the navigation layer can pass it to ActivateAccount.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success(val email: String) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

/**
 * ViewModel for the registration screen.
 * Calls [RegisterUseCase] and exposes the result as [RegisterUiState].
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            registerUseCase(firstName, lastName, username, email, password, confirmPassword)
                .onSuccess { _uiState.value = RegisterUiState.Success(email) }
                .onFailure {
                    _uiState.value = RegisterUiState.Error(it.message ?: "Registration failed")
                }
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}
