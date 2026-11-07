package com.burgerhouse.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Pedido finalizado (persistido para o histórico).
 * O status é um índice de [com.burgerhouse.app.domain.model.OrderStatus].
 */
@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val createdAt: Long,
    val subtotal: Double,
    val deliveryFee: Double,
    val discount: Double,
    val total: Double,
    val address: String,
    val paymentMethod: String,
    /** Troco solicitado (pagamento em dinheiro), null quando não aplicável. */
    val changeFor: Double? = null,
    val statusIndex: Int = 0,
    val estimatedMinutes: Int = 35,
    /** Pontos de fidelidade gerados pelo pedido. */
    val loyaltyPoints: Int = 0
)

/** Forma de pagamento usada em [Order.paymentMethod]. */
object PaymentMethods {
    const val CASH = "Dinheiro"
    const val CREDIT_CARD = "Cartão"
    const val PIX = "Pix"
}
