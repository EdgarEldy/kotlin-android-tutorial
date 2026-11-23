package com.edgareldy.tutorial.features.cart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgareldy.tutorial.domain.usecase.cart.GetCartItemCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Shared ViewModel scoped to the main navigation host that tracks the cart
 * item count for the bottom navigation badge. It is instantiated once at the
 * MainScreen level and survives tab switches, keeping the badge up to date
 * across the entire authenticated graph.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@HiltViewModel
class SharedCartViewModel @Inject constructor(
    private val getCartItemCountUseCase: GetCartItemCountUseCase
) : ViewModel() {

    private val _cartItemCount = MutableStateFlow(0)
    val cartItemCount: StateFlow<Int> = _cartItemCount.asStateFlow()

    init {
        refreshCount()
    }

    fun refreshCount() {
        viewModelScope.launch {
            _cartItemCount.value = getCartItemCountUseCase()
        }
    }
}
