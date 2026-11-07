package com.burgerhouse.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.burgerhouse.app.BurgerHouseApp
import com.burgerhouse.app.data.local.entity.CartItem
import com.burgerhouse.app.data.local.entity.OrderWithItems
import com.burgerhouse.app.data.repository.BurgerRepository
import com.burgerhouse.app.domain.model.OrderStatus
import com.burgerhouse.app.ui.navigation.SnackbarController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Histórico de pedidos e acompanhamento em tempo real.
 * O status do pedido avança automaticamente a cada 15 segundos
 * (simulação de atualização do restaurante).
 */
class OrderViewModel(
    private val repository: BurgerRepository
) : ViewModel() {

    /** Lista de pedidos (com itens) do histórico. */
    val orders: StateFlow<List<OrderWithItems>> = repository.getOrdersWithItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentOrder = MutableStateFlow<OrderWithItems?>(null)
    val currentOrder: StateFlow<OrderWithItems?> = _currentOrder.asStateFlow()

    private var trackingJob: Job? = null

    /** Inicia (ou reinicia) o acompanhamento de um pedido. */
    fun startTracking(orderId: Long) {
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch {
            repository.getOrderWithItems(orderId).collect { orderWithItems ->
                _currentOrder.value = orderWithItems

                // Avança o status automaticamente a cada 15s (simulado)
                val order = orderWithItems?.order ?: return@collect
                if (order.statusIndex < OrderStatus.DELIVERED.step) {
                    delay(STATUS_UPDATE_INTERVAL)
                    repository.updateOrder(order.copy(statusIndex = order.statusIndex + 1))
                }
            }
        }
    }

    fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
        _currentOrder.value = null
    }

    /** "Pedir novamente": adiciona os itens do pedido ao carrinho. */
    fun reorder(order: OrderWithItems) {
        viewModelScope.launch {
            order.items.forEach { item ->
                // Recupera a imagem atual do produto para exibir no carrinho
                val product = repository.getProductById(item.productId).first()
                repository.addToCart(
                    CartItem(
                        productId = item.productId,
                        name = item.name,
                        imageUrl = product?.imageUrl ?: "",
                        unitPrice = item.unitPrice,
                        quantity = item.quantity,
                        isBurger = product?.category == "BURGERS",
                        notes = item.notes
                    )
                )
            }
            SnackbarController.show(
                "Itens do pedido ${order.order.orderNumber} adicionados ao carrinho"
            )
        }
    }

    fun deleteOrder(orderId: Long) {
        viewModelScope.launch {
            repository.deleteOrder(orderId)
            SnackbarController.show("Pedido removido do histórico")
        }
    }

    override fun onCleared() {
        trackingJob?.cancel()
        super.onCleared()
    }

    companion object {
        /** Intervalo (ms) de atualização automática de status. */
        const val STATUS_UPDATE_INTERVAL = 15_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as BurgerHouseApp
                OrderViewModel(app.repository)
            }
        }
    }
}
