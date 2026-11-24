package com.edgareldy.tutorial.cart.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgareldy.tutorial.cart.domain.model.Cart
import com.edgareldy.tutorial.cart.domain.usecases.ClearCartUseCase
import com.edgareldy.tutorial.cart.domain.usecases.GetCartUseCase
import com.edgareldy.tutorial.cart.domain.usecases.RemoveCartItemUseCase
import com.edgareldy.tutorial.cart.domain.usecases.UpdateCartItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the cart screen. Follows the sealed-class pattern used across
 * all screens: Loading, Success, Empty and Error are mutually exclusive so the
 * Composable can use a when expression without an else branch.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
sealed class CartUiState {
    data object Loading : CartUiState()
    data class Success(val cart: Cart) : CartUiState()
    data object Empty : CartUiState()
    data class Error(val message: String) : CartUiState()
}

/**
 * ViewModel for the CartScreen. Coordinates all cart mutations through use cases
 * and exposes a single StateFlow<CartUiState> so the Composable stays stateless.
 * decreaseQuantity delegates to RemoveCartItemUseCase when quantity reaches 1,
 * avoiding a quantity-0 state that the backend rejects.
 * cartMutationEvent is a SharedFlow used to signal SharedCartViewModel to refresh the badge.
 *
 * Created edgar.muhamyangabo on 11/24/26
 * Author: edgar.muhamyangabo
 * Date: 11/24/26
 * Project: kotlin-android-tutorial
 */
@HiltViewModel
class CartViewModel @Inject constructor(
    private val getCartUseCase: GetCartUseCase,
    private val updateCartItemUseCase: UpdateCartItemUseCase,
    private val removeCartItemUseCase: RemoveCartItemUseCase,
    private val clearCartUseCase: ClearCartUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CartUiState>(CartUiState.Loading)
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _cartMutationEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val cartMutationEvent: SharedFlow<Unit> = _cartMutationEvent.asSharedFlow()

    fun loadCart() {
        viewModelScope.launch {
            _uiState.value = CartUiState.Loading
            getCartUseCase().fold(
                onSuccess = { cart ->
                    _uiState.value = if (cart.items.isEmpty()) CartUiState.Empty
                    else CartUiState.Success(cart)
                },
                onFailure = { error ->
                    _uiState.value = CartUiState.Error(error.message ?: "Failed to load cart")
                }
            )
        }
    }

    fun increaseQuantity(itemId: Long, currentQuantity: Int) {
        viewModelScope.launch {
            updateCartItemUseCase(itemId, currentQuantity + 1).fold(
                onSuccess = { cart ->
                    _uiState.value = if (cart.items.isEmpty()) CartUiState.Empty
                    else CartUiState.Success(cart)
                    _cartMutationEvent.tryEmit(Unit)
                },
                onFailure = { error ->
                    _snackbarMessage.value = error.message ?: "Failed to update quantity"
                }
            )
        }
    }

    fun decreaseQuantity(itemId: Long, currentQuantity: Int) {
        viewModelScope.launch {
            if (currentQuantity > 1) {
                updateCartItemUseCase(itemId, currentQuantity - 1).fold(
                    onSuccess = { cart ->
                        _uiState.value = if (cart.items.isEmpty()) CartUiState.Empty
                        else CartUiState.Success(cart)
                        _cartMutationEvent.tryEmit(Unit)
                    },
                    onFailure = { error ->
                        _snackbarMessage.value = error.message ?: "Failed to update quantity"
                    }
                )
            } else {
                removeItem(itemId)
            }
        }
    }

    fun removeItem(itemId: Long) {
        viewModelScope.launch {
            removeCartItemUseCase(itemId).fold(
                onSuccess = { cart ->
                    _uiState.value = if (cart.items.isEmpty()) CartUiState.Empty
                    else CartUiState.Success(cart)
                    _cartMutationEvent.tryEmit(Unit)
                },
                onFailure = { error ->
                    _snackbarMessage.value = error.message ?: "Failed to remove item"
                }
            )
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            clearCartUseCase().fold(
                onSuccess = {
                    _uiState.value = CartUiState.Empty
                    _cartMutationEvent.tryEmit(Unit)
                },
                onFailure = { error ->
                    _snackbarMessage.value = error.message ?: "Failed to clear cart"
                }
            )
        }
    }

    fun snackbarMessageShown() {
        _snackbarMessage.value = null
    }
}
