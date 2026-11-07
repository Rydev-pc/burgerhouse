package com.burgerhouse.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.burgerhouse.app.BurgerHouseApp
import com.burgerhouse.app.data.local.entity.CartItem
import com.burgerhouse.app.data.local.entity.Coupon
import com.burgerhouse.app.data.local.entity.Order
import com.burgerhouse.app.data.local.entity.OrderItem
import com.burgerhouse.app.data.local.entity.PaymentMethods
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
import kotlin.math.floor

/**
 * Checkout: endereço de entrega, forma de pagamento, resumo do pedido
 * e finalização (persistência do pedido + pontos de fidelidade).
 */
class CheckoutViewModel(
    private val repository: BurgerRepository
) : ViewModel() {

    private val _appliedCoupon = MutableStateFlow<Coupon?>(null)
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon

    private val _couponError = MutableStateFlow<String?>(null)
    val couponError: StateFlow<String?> = _couponError

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address

    private val _paymentMethod = MutableStateFlow(PaymentMethods.CREDIT_CARD)
    val paymentMethod: StateFlow<String> = _paymentMethod

    private val _changeFor = MutableStateFlow("")
    val changeFor: StateFlow<String> = _changeFor

    /** Resumo recalculado a partir do carrinho + cupom aplicado. */
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
            couponCode = coupon?.code?.takeIf { coupon.active },
            itemCount = items.sumOf { it.quantity }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CartSummary())

    val cartItems: StateFlow<List<CartItem>> = repository.getCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _orderCreatedId = MutableStateFlow<Long?>(null)
    val orderCreatedId: StateFlow<Long?> = _orderCreatedId

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting

    init {
        // Pré-preenche o endereço com o do usuário logado
        viewModelScope.launch {
            val user = repository.getUser().first()
            if (user != null) _address.value = user.address
        }
    }

    fun onAddressChange(value: String) {
        _address.value = value
    }

    fun selectPaymentMethod(method: String) {
        _paymentMethod.value = method
        // Limpa o campo de troco se não for pagamento em dinheiro
        if (method != PaymentMethods.CASH) _changeFor.value = ""
    }

    fun onChangeFor(value: String) {
        // Aceita apenas dígitos e separador decimal
        if (value.all { it.isDigit() || it == ',' || it == '.' }) {
            _changeFor.value = value
        }
    }

    fun applyCoupon(code: String) {
        viewModelScope.launch {
            val clean = code.uppercase().trim()
            if (clean.isBlank()) {
                _couponError.value = "Digite um cupom."
                return@launch
            }
            val coupon = repository.findCoupon(clean)
            if (coupon == null || !coupon.active) {
                _couponError.value = "Cupom inválido ou expirado."
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

    /**
     * Finaliza o pedido: cria o pedido e seus itens, limpa o carrinho,
     * credita pontos de fidelidade e informa o ID criado.
     */
    fun confirmOrder() {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val items = repository.getCartItems().first()
                if (items.isEmpty()) {
                    SnackbarController.show("Carrinho vazio. Adicione itens antes de finalizar.")
                    return@launch
                }

                val currentSummary = summary.value
                val orderNumber = "#%04d".format((1000..9999).random())
                val loyaltyPoints =
                    floor(currentSummary.total / POINTS_PER_AMOUNT).toInt()

                val order = Order(
                    orderNumber = orderNumber,
                    createdAt = System.currentTimeMillis(),
                    subtotal = currentSummary.subtotal,
                    deliveryFee = currentSummary.deliveryFee,
                    discount = currentSummary.discount,
                    total = currentSummary.total,
                    address = _address.value.trim(),
                    paymentMethod = _paymentMethod.value,
                    changeFor = parseMoney(_changeFor.value),
                    loyaltyPoints = loyaltyPoints
                )

                val orderItems = items.map { item ->
                    OrderItem(
                        orderId = 0L,
                        productId = item.productId,
                        name = item.name,
                        quantity = item.quantity,
                        unitPrice = item.unitPrice,
                        extrasPrice = item.extrasPrice,
                        notes = buildItemDescription(item)
                    )
                }

                val id = repository.placeOrder(order, orderItems)
                if (loyaltyPoints > 0) repository.addLoyaltyPoints(loyaltyPoints)

                SnackbarController.show("Pedido $orderNumber confirmado!")
                _orderCreatedId.value = id
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /** Converte "12,50"/"12.50" em Double; retorna null quando vazio. */
    private fun parseMoney(value: String): Double? =
        value.replace(",", ".").toDoubleOrNull()?.takeIf { it > 0.0 }

    /** Monta a descrição textual dos adicionais/ponto/observações. */
    private fun buildItemDescription(item: CartItem): String {
        val parts = mutableListOf<String>()
        if (item.bacon) parts.add("Bacon")
        if (item.extraCheese) parts.add("Cheddar extra")
        if (item.egg) parts.add("Ovo")
        if (item.caramelizedOnion) parts.add("Cebola caramelizada")
        if (item.isBurger) parts.add("Ponto: ${item.meatPoint}")
        if (item.notes.isNotBlank()) parts.add("Obs: ${item.notes}")
        return parts.joinToString(" | ")
    }

    fun consumeOrderCreated() {
        _orderCreatedId.value = null
    }

    companion object {
        const val DELIVERY_FEE = 8.0

        /** A cada R$ 50,00 em compras o cliente ganha 1 ponto. */
        const val POINTS_PER_AMOUNT = 50.0

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as BurgerHouseApp
                CheckoutViewModel(app.repository)
            }
        }
    }
}
