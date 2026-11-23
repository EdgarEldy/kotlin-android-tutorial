package com.edgareldy.tutorial.features.order.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgareldy.tutorial.domain.model.Order
import com.edgareldy.tutorial.domain.usecase.order.GetOrderByIdUseCase
import com.edgareldy.tutorial.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the order detail screen.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
sealed class OrderDetailUiState {
    object Loading : OrderDetailUiState()
    data class Success(val order: Order) : OrderDetailUiState()
    data class Error(val message: String) : OrderDetailUiState()
}

/**
 * ViewModel for the order detail screen.
 * Reads the orderId navigation argument from SavedStateHandle so the
 * ViewModel is correctly scoped to the NavBackStackEntry and survives
 * process death and configuration changes.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val getOrderByIdUseCase: GetOrderByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId: Long = checkNotNull(
        savedStateHandle[Screen.OrderDetail.ARG_ORDER_ID]
    )

    private val _uiState = MutableStateFlow<OrderDetailUiState>(OrderDetailUiState.Loading)
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    init {
        loadOrder()
    }

    private fun loadOrder() {
        viewModelScope.launch {
            getOrderByIdUseCase(orderId).collect { result ->
                result.fold(
                    onSuccess = { order ->
                        _uiState.value = OrderDetailUiState.Success(order)
                    },
                    onFailure = { error ->
                        _uiState.value = OrderDetailUiState.Error(
                            error.message ?: "Failed to load order"
                        )
                    }
                )
            }
        }
    }
}
