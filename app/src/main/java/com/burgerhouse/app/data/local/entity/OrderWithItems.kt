package com.burgerhouse.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Relação 1:N entre pedido e seus itens (usada com @Transaction no DAO).
 */
data class OrderWithItems(
    @Embedded val order: Order,
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val items: List<OrderItem>
)
