package com.edgareldy.tutorial.features.products.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edgareldy.tutorial.domain.model.Category
import com.edgareldy.tutorial.domain.model.Product
import com.edgareldy.tutorial.domain.usecase.product.GetCategoriesUseCase
import com.edgareldy.tutorial.domain.usecase.product.GetProductsUseCase
import com.edgareldy.tutorial.domain.usecase.product.RefreshProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the product list screen.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
sealed class ProductListUiState {
    object Loading : ProductListUiState()
    data class Success(
        val products: List<Product>,
        val categories: List<Category>,
        val selectedCategoryId: Long?
    ) : ProductListUiState()
    data class Error(val message: String) : ProductListUiState()
    data class Empty(
        val categories: List<Category>,
        val selectedCategoryId: Long?
    ) : ProductListUiState()
}

/**
 * ViewModel for the product list screen. Combines products and categories into
 * a single [ProductListUiState]. The search query is debounced to avoid
 * triggering a new flow collection on every keystroke.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val refreshProductsUseCase: RefreshProductsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductListUiState>(ProductListUiState.Loading)
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        observeProducts()
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun observeProducts() {
        viewModelScope.launch {
            val debouncedSearch = _searchQuery.debounce(300L)

            combine(
                _selectedCategoryId,
                debouncedSearch
            ) { categoryId, search -> Pair(categoryId, search) }
                .flatMapLatest { (categoryId, search) ->
                    combine(
                        getProductsUseCase(categoryId = categoryId, search = search.ifBlank { null }),
                        getCategoriesUseCase()
                    ) { productsResult, categoriesResult ->
                        val products = productsResult.getOrElse { emptyList() }
                        val categories = categoriesResult.getOrElse { emptyList() }

                        when {
                            productsResult.isFailure && products.isEmpty() ->
                                ProductListUiState.Error(
                                    productsResult.exceptionOrNull()?.message ?: "Failed to load products"
                                )
                            products.isEmpty() -> ProductListUiState.Empty(
                                categories = categories,
                                selectedCategoryId = categoryId
                            )
                            else -> ProductListUiState.Success(
                                products = products,
                                categories = categories,
                                selectedCategoryId = categoryId
                            )
                        }
                    }
                }
                .collectLatest { state ->
                    _uiState.value = state
                }
        }
    }

    fun selectCategory(id: Long?) {
        _selectedCategoryId.value = id
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            runCatching {
                refreshProductsUseCase(_selectedCategoryId.value)
            }
            _isRefreshing.value = false
        }
    }
}
