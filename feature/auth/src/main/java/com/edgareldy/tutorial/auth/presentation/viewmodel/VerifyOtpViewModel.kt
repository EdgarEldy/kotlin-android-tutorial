package com.edgareldy.tutorial.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgareldy.tutorial.auth.domain.usecase.VerifyPasswordResetOtpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the OTP verification screen.
 * Success carries the verified OTP value so it can be forwarded to the
 * ResetPassword route. The OTP is never logged here.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
sealed class VerifyOtpUiState {
    object Idle : VerifyOtpUiState()
    object Loading : VerifyOtpUiState()
    data class Success(val otp: String) : VerifyOtpUiState()
    data class Error(val message: String) : VerifyOtpUiState()
}

/**
 * ViewModel for the OTP verification screen (step 2 of password reset).
 * Calls [VerifyPasswordResetOtpUseCase] without logging the OTP value.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@HiltViewModel
class VerifyOtpViewModel @Inject constructor(
    private val verifyPasswordResetOtpUseCase: VerifyPasswordResetOtpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<VerifyOtpUiState>(VerifyOtpUiState.Idle)
    val uiState: StateFlow<VerifyOtpUiState> = _uiState.asStateFlow()

    fun verifyOtp(email: String, otp: String) {
        viewModelScope.launch {
            _uiState.value = VerifyOtpUiState.Loading
            verifyPasswordResetOtpUseCase(email, otp)
                .onSuccess { _uiState.value = VerifyOtpUiState.Success(otp) }
                .onFailure {
                    _uiState.value = VerifyOtpUiState.Error(it.message ?: "Invalid OTP")
                }
        }
    }

    fun resetState() {
        _uiState.value = VerifyOtpUiState.Idle
    }
}
