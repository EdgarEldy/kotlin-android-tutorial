package com.edgareldy.tutorial.features.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgareldy.tutorial.domain.usecase.auth.ActivateAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the account activation screen.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
sealed class ActivateAccountUiState {
    object Idle : ActivateAccountUiState()
    object Loading : ActivateAccountUiState()
    object Success : ActivateAccountUiState()
    data class Error(val message: String) : ActivateAccountUiState()
}

/**
 * ViewModel for the account activation screen.
 * Calls [ActivateAccountUseCase] with the email and 6-digit code.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@HiltViewModel
class ActivateAccountViewModel @Inject constructor(
    private val activateAccountUseCase: ActivateAccountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ActivateAccountUiState>(ActivateAccountUiState.Idle)
    val uiState: StateFlow<ActivateAccountUiState> = _uiState.asStateFlow()

    fun activate(email: String, activationCode: String) {
        viewModelScope.launch {
            _uiState.value = ActivateAccountUiState.Loading
            activateAccountUseCase(email, activationCode)
                .onSuccess { _uiState.value = ActivateAccountUiState.Success }
                .onFailure {
                    _uiState.value = ActivateAccountUiState.Error(
                        it.message ?: "Activation failed"
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = ActivateAccountUiState.Idle
    }
}
