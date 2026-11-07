package com.burgerhouse.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.burgerhouse.app.BurgerHouseApp
import com.burgerhouse.app.data.repository.BurgerRepository
import com.burgerhouse.app.domain.model.ProductCategory
import com.burgerhouse.app.ui.navigation.SnackbarController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel do cardápio (Home) e busca no cardápio.
 * Expõe produtos filtrados por categoria e/ou texto (com debounce).
 */
class MenuViewModel(
    private val repository: BurgerRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ProductCategory?>(null)
    val selectedCategory: StateFlow<ProductCategory?> = _selectedCategory.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Lista de produtos filtrada (categoria + busca com debounce de 300ms). */
    val products: StateFlow<List<com.burgerhouse.app.data.local.entity.Product>> =
        combine(
            repository.getProducts(),
            _query.debounce(300),
            _selectedCategory
        ) { all, query, category ->
            var filtered = all
            if (category != null) {
                filtered = filtered.filter { it.category == category.id }
            }
            if (query.isNotBlank()) {
                filtered = filtered.filter {
                    it.name.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
                }
            }
            filtered
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Quantidade total de itens no carrinho (badge do header). */
    val cartCount: StateFlow<Int> = repository.getCartItemCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _selectedProduct = MutableStateFlow<com.burgerhouse.app.data.local.entity.Product?>(null)
    val selectedProduct: StateFlow<com.burgerhouse.app.data.local.entity.Product?> = _selectedProduct.asStateFlow()

    init {
        // Assim que houver a primeira emissão, encerra o estado de loading
        viewModelScope.launch {
            repository.getProducts().collect {
                _isLoading.value = false
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun clearQuery() {
        _query.value = ""
    }

    fun selectCategory(category: ProductCategory?) {
        _selectedCategory.value = category
    }

    fun loadProduct(productId: Long) {
        viewModelScope.launch {
            _selectedProduct.value = repository.getProductById(productId).first()
        }
    }

    /** Adiciona um produto simples ao carrinho (usado pelo botão rápido da Home). */
    fun quickAddToCart(product: com.burgerhouse.app.data.local.entity.Product) {
        viewModelScope.launch {
            repository.addToCart(
                com.burgerhouse.app.data.local.entity.CartItem(
                    productId = product.id,
                    name = product.name,
                    imageUrl = product.imageUrl,
                    unitPrice = product.price,
                    quantity = 1,
                    isBurger = product.category == ProductCategory.BURGERS.id
                )
            )
            SnackbarController.show("${product.name} adicionado ao carrinho")
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as BurgerHouseApp
                MenuViewModel(app.repository)
            }
        }
    }
}
