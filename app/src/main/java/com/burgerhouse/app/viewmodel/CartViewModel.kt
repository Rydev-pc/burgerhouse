package com.burgerhouse.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.burgerhouse.app.BurgerHouseApp
import com.burgerhouse.app.data.local.entity.CartItem
import com.burgerhouse.app.data.local.entity.Coupon
import com.burgerhouse.app.data.repository.BurgerRepository
import com.burgerhouse.app.domain.model.CartSummary
import com.burgerhouse.app.ui.navigation.SnackbarController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Carrinho de compras: CRUD dos itens, cálculo automático de totais
 * e aplicação de cupons de desconto.
 */
class CartViewModel(
    private val repository: BurgerRepository
) : ViewModel() {

    private val _appliedCoupon = MutableStateFlow<Coupon?>(null)
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon

    private val _couponError = MutableStateFlow<String?>(null)
    val couponError: StateFlow<String?> = _couponError

    val cartItems: StateFlow<List<CartItem>> = repository.getCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Resumo financeiro (subtotal, desconto, entrega, total). */
    val summary: StateFlow<CartSummary> = combine(
        repository.getCartItems(),
        _appliedCoupon
    ) { items, coupon ->
        val subtotal = items.sumOf { it.totalPrice }
        val freeShipping = coupon?.type == Coupon.TYPE_FREESHIP
        val discount = if (coupon?.type == Coupon.TYPE_PERCENT) {
            subtotal * coupon.value / 100.0
        } else 0.0
        CartSummary(
            subtotal = subtotal,
            deliveryFee = if (items.isEmpty() || freeShipping) 0.0 else DELIVERY_FEE,
            discount = discount,
            freeShipping = freeShipping,
            couponCode = if (coupon?.code != null && coupon.active) coupon.code else null,
            itemCount = items.sumOf { it.quantity }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CartSummary())

    /** Adiciona item já com adicionais/ponto/observações definidos na tela de detalhe. */
    fun addToCart(item: CartItem) {
        viewModelScope.launch {
            repository.addToCart(item)
            SnackbarController.show("${item.name} adicionado ao carrinho")
        }
    }

    fun incrementQuantity(item: CartItem) {
        viewModelScope.launch {
            repository.updateCartItem(item.copy(quantity = item.quantity + 1))
        }
    }

    fun decrementQuantity(item: CartItem) {
        viewModelScope.launch {
            if (item.quantity > 1) {
                repository.updateCartItem(item.copy(quantity = item.quantity - 1))
            } else {
                repository.removeFromCart(item)
                SnackbarController.show("${item.name} removido do carrinho")
            }
        }
    }

    fun removeItem(item: CartItem) {
        viewModelScope.launch {
            repository.removeFromCart(item)
            SnackbarController.show("${item.name} removido do carrinho")
        }
    }

    /** Valida e aplica o cupom (erro acessível em [couponError]). */
    fun applyCoupon(code: String) {
        viewModelScope.launch {
            val cleanCode = code.uppercase().trim()
            if (cleanCode.isBlank()) {
                _couponError.value = "Digite um cupom."
                return@launch
            }
            val coupon = repository.findCoupon(cleanCode)
            if (coupon == null || !coupon.active) {
                _couponError.value = "Cupom inválido ou expirado."
                _appliedCoupon.value = null
            } else {
                _appliedCoupon.value = coupon
                _couponError.value = null
                SnackbarController.show("Cupom ${coupon.code} aplicado!")
            }
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
        _couponError.value = null
    }

    suspend fun currentItems(): List<CartItem> = repository.getCartItems().first()

    companion object {
        /** Taxa de entrega fixa do app. */
        const val DELIVERY_FEE = 8.0

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as BurgerHouseApp
                CartViewModel(app.repository)
            }
        }
    }
}
