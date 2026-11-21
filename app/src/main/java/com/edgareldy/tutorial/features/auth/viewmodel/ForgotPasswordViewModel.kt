package com.edgareldy.tutorial.features.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgareldy.tutorial.domain.usecase.auth.SendPasswordResetOtpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the forgot-password screen.
 * Success carries the email to pass it to the VerifyOtp route.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
sealed class ForgotPasswordUiState {
    object Idle : ForgotPasswordUiState()
    object Loading : ForgotPasswordUiState()
    data class Success(val email: String) : ForgotPasswordUiState()
    data class Error(val message: String) : ForgotPasswordUiState()
}

/**
 * ViewModel for the forgot-password screen.
 * Calls [SendPasswordResetOtpUseCase] to request a password reset OTP.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val sendPasswordResetOtpUseCase: SendPasswordResetOtpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ForgotPasswordUiState>(ForgotPasswordUiState.Idle)
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun sendOtp(email: String) {
        viewModelScope.launch {
            _uiState.value = ForgotPasswordUiState.Loading
            sendPasswordResetOtpUseCase(email)
                .onSuccess { _uiState.value = ForgotPasswordUiState.Success(email) }
                .onFailure {
                    _uiState.value = ForgotPasswordUiState.Error(
                        it.message ?: "Failed to send OTP"
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = ForgotPasswordUiState.Idle
    }
}
