package com.edgareldy.tutorial.orders.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgareldy.tutorial.orders.domain.usecases.CreateOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the checkout confirmation screen.
 * Idle is the initial state before the user taps the confirm button.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
sealed class CheckoutUiState {
    data object Idle : CheckoutUiState()
    data object Loading : CheckoutUiState()
    data class Success(val orderId: Long) : CheckoutUiState()
    data class Error(val message: String) : CheckoutUiState()
}

/**
 * ViewModel for the checkout confirmation screen. confirmOrder() calls
 * CreateOrderUseCase and transitions from Idle through Loading to Success
 * or Error. The screen navigates to OrderDetail on Success using the
 * returned orderId.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
@HiltViewModel
class CheckoutConfirmationViewModel @Inject constructor(
    private val createOrderUseCase: CreateOrderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckoutUiState>(CheckoutUiState.Idle)
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    fun confirmOrder() {
        if (_uiState.value is CheckoutUiState.Loading) return
        viewModelScope.launch {
            _uiState.value = CheckoutUiState.Loading
            createOrderUseCase().fold(
                onSuccess = { order ->
                    _uiState.value = CheckoutUiState.Success(order.id)
                },
                onFailure = { error ->
                    _uiState.value = CheckoutUiState.Error(
                        error.message ?: "Checkout failed"
                    )
                }
            )
        }
    }

    fun successHandled() {
        if (_uiState.value is CheckoutUiState.Success) {
            _uiState.value = CheckoutUiState.Idle
        }
    }

    fun errorShown() {
        if (_uiState.value is CheckoutUiState.Error) {
            _uiState.value = CheckoutUiState.Idle
        }
    }
}
