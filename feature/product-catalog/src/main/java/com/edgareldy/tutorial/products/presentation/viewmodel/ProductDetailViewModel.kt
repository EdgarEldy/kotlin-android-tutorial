package com.edgareldy.tutorial.products.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgareldy.tutorial.cart.domain.usecases.AddCartItemUseCase
import com.edgareldy.tutorial.products.domain.model.Product
import com.edgareldy.tutorial.products.domain.usecases.GetProductByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the product detail screen.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
sealed class ProductDetailUiState {
    object Loading : ProductDetailUiState()
    data class Success(val product: Product) : ProductDetailUiState()
    data class Error(val message: String) : ProductDetailUiState()
}

/**
 * ViewModel for the product detail screen.
 * Reads the productId from [SavedStateHandle] so the argument survives
 * process death without additional manual restoration logic.
 * Also handles the add-to-cart action via [AddCartItemUseCase] and exposes
 * a one-shot snackbar message for feedback.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val addCartItemUseCase: AddCartItemUseCase
) : ViewModel() {

    private val productId: Long = savedStateHandle.get<Long>("productId") ?: 0L

    private val _uiState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _addToCartLoading = MutableStateFlow(false)
    val addToCartLoading: StateFlow<Boolean> = _addToCartLoading.asStateFlow()

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            getProductByIdUseCase(productId).collectLatest { result ->
                _uiState.value = result.fold(
                    onSuccess = { ProductDetailUiState.Success(it) },
                    onFailure = { ProductDetailUiState.Error(it.message ?: "Failed to load product") }
                )
            }
        }
    }

    fun retry() {
        _uiState.value = ProductDetailUiState.Loading
        loadProduct()
    }

    fun addToCart(quantity: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _addToCartLoading.value = true
            addCartItemUseCase(productId, quantity).fold(
                onSuccess = {
                    _snackbarMessage.value = "Ajoute au panier"
                    onSuccess()
                },
                onFailure = { error ->
                    _snackbarMessage.value = error.message ?: "Erreur reseau"
                }
            )
            _addToCartLoading.value = false
        }
    }

    fun snackbarMessageShown() {
        _snackbarMessage.value = null
    }
}
