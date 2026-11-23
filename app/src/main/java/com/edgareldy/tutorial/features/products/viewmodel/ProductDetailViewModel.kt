package com.edgareldy.tutorial.features.products.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgareldy.tutorial.domain.model.Product
import com.edgareldy.tutorial.domain.usecase.product.GetProductByIdUseCase
import com.edgareldy.tutorial.navigation.Screen
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
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProductByIdUseCase: GetProductByIdUseCase
) : ViewModel() {

    private val productId: Long =
        savedStateHandle.get<Long>(Screen.ProductDetail.ARG_PRODUCT_ID) ?: 0L

    private val _uiState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

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
}
