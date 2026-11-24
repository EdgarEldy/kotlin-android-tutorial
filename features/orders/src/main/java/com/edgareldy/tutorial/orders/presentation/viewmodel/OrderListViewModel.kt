package com.edgareldy.tutorial.orders.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgareldy.tutorial.orders.domain.model.Order
import com.edgareldy.tutorial.orders.domain.usecases.GetOrdersUseCase
import com.edgareldy.tutorial.orders.domain.usecases.RefreshOrdersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the order list screen.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
sealed class OrderListUiState {
    data object Loading : OrderListUiState()
    data class Success(val orders: List<Order>) : OrderListUiState()
    data class Error(val message: String) : OrderListUiState()
    data object Empty : OrderListUiState()
}

/**
 * ViewModel for the order list screen. Collects GetOrdersUseCase on init and
 * exposes a pull-to-refresh entry point via refresh(). isRefreshing tracks
 * the network-side refresh separately from the initial loading state.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
@HiltViewModel
class OrderListViewModel @Inject constructor(
    private val getOrdersUseCase: GetOrdersUseCase,
    private val refreshOrdersUseCase: RefreshOrdersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrderListUiState>(OrderListUiState.Loading)
    val uiState: StateFlow<OrderListUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadOrders()
    }

    private fun loadOrders() {
        viewModelScope.launch {
            getOrdersUseCase().collect { result ->
                result.fold(
                    onSuccess = { orders ->
                        _uiState.value = if (orders.isEmpty()) OrderListUiState.Empty
                        else OrderListUiState.Success(orders)
                    },
                    onFailure = { error ->
                        _uiState.value = OrderListUiState.Error(
                            error.message ?: "Failed to load orders"
                        )
                    }
                )
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            refreshOrdersUseCase()
            _isRefreshing.value = false
        }
    }
}
