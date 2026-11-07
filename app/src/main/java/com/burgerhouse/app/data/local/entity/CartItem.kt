package com.burgerhouse.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Item do carrinho. Guarda uma "foto" do produto no momento da adição
 * (nome, imagem, preço unitário) para evitar problemas de atualização
 * de preço no cardápio após o item já estar no carrinho.
 *
 * O preço total do item é calculado dinamicamente:
 *   (unitPrice + precoDosAdicionais) * quantidade
 */
@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val name: String,
    val imageUrl: String,
    val unitPrice: Double,
    var quantity: Int,
    val isBurger: Boolean = true,
    // Adicionais
    val bacon: Boolean = false,
    val extraCheese: Boolean = false,
    val egg: Boolean = false,
    val caramelizedOnion: Boolean = false,
    // Ponto da carne (apenas hambúrgueres)
    val meatPoint: String = "Ao ponto",
    val notes: String = ""
) {
    /** Preço somado dos adicionais selecionados (preços fixos do app). */
    val extrasPrice: Double
        get() = (if (bacon) 4.0 else 0.0) +
            (if (extraCheese) 3.5 else 0.0) +
            (if (egg) 2.5 else 0.0) +
            (if (caramelizedOnion) 3.0 else 0.0)

    /** Preço total do item já considerando adicionais e quantidade. */
    val totalPrice: Double
        get() = (unitPrice + extrasPrice) * quantity
}
