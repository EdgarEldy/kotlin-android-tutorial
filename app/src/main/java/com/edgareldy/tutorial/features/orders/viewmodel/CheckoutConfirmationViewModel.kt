package com.edgareldy.tutorial.features.orders.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgareldy.tutorial.domain.usecase.order.CreateOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the checkout confirmation screen.
 * Idle is the initial state before the user taps Confirm.
 * Loading disables the button and shows a spinner.
 * Success carries the new order id for navigation to OrderDetail.
 * Error surfaces the backend message in a Snackbar.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
sealed class CheckoutUiState {
    object Idle : CheckoutUiState()
    object Loading : CheckoutUiState()
    data class Success(val orderId: Long) : CheckoutUiState()
    data class Error(val message: String) : CheckoutUiState()
}

/**
 * ViewModel for the checkout confirmation screen.
 * confirmOrder delegates to CreateOrderUseCase which always requires the
 * network. On success the state transitions to Success(orderId) so the
 * Composable can navigate to OrderDetail without holding any extra state.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
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

    fun errorShown() {
        if (_uiState.value is CheckoutUiState.Error) {
            _uiState.value = CheckoutUiState.Idle
        }
    }
}
