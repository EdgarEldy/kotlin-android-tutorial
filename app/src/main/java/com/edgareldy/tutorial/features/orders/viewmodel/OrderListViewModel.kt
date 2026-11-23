package com.edgareldy.tutorial.features.orders.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgareldy.tutorial.domain.model.Order
import com.edgareldy.tutorial.domain.usecase.order.GetOrdersUseCase
import com.edgareldy.tutorial.domain.usecase.order.RefreshOrdersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the order list screen.
 * Loading shows a spinner on first open, Success shows the list,
 * Empty renders a placeholder, Error shows the backend message.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
sealed class OrderListUiState {
    object Loading : OrderListUiState()
    data class Success(val orders: List<Order>) : OrderListUiState()
    object Empty : OrderListUiState()
    data class Error(val message: String) : OrderListUiState()
}

/**
 * ViewModel for the order list screen. Collects the cache-first flow from
 * GetOrdersUseCase and exposes a single StateFlow<OrderListUiState>.
 * Pull-to-refresh is handled by RefreshOrdersUseCase which forces an
 * immediate network call and writes results into Room, automatically
 * re-triggering the collector.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
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
            runCatching { refreshOrdersUseCase() }
            _isRefreshing.value = false
        }
    }
}
