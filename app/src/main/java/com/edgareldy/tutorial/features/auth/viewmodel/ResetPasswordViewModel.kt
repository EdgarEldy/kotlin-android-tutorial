package com.edgareldy.tutorial.features.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgareldy.tutorial.domain.usecase.auth.ConfirmPasswordResetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the reset-password screen.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
sealed class ResetPasswordUiState {
    object Idle : ResetPasswordUiState()
    object Loading : ResetPasswordUiState()
    object Success : ResetPasswordUiState()
    data class Error(val message: String) : ResetPasswordUiState()
}

/**
 * ViewModel for the reset-password screen (step 3 of the password reset flow).
 * Calls [ConfirmPasswordResetUseCase] using the OTP received from the previous screen.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val confirmPasswordResetUseCase: ConfirmPasswordResetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResetPasswordUiState>(ResetPasswordUiState.Idle)
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    fun resetPassword(otp: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            _uiState.value = ResetPasswordUiState.Loading
            confirmPasswordResetUseCase(otp, newPassword, confirmPassword)
                .onSuccess { _uiState.value = ResetPasswordUiState.Success }
                .onFailure {
                    _uiState.value = ResetPasswordUiState.Error(
                        it.message ?: "Password reset failed"
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = ResetPasswordUiState.Idle
    }
}
