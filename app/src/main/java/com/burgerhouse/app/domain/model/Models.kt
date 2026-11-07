package com.burgerhouse.app.domain.model

/**
 * Categorias do cardápio exibidas como chips na Home.
 * O [id] é o valor persistido no banco (Room).
 */
enum class ProductCategory(val id: String, val title: String) {
    BURGERS("BURGERS", "Hambúrgueres"),
    COMBOS("COMBOS", "Combos"),
    SIDES("SIDES", "Acompanhamentos"),
    DRINKS("DRINKS", "Bebidas"),
    DESSERTS("DESSERTS", "Sobremesas");

    companion object {
        fun fromId(id: String): ProductCategory =
            entries.firstOrNull { it.id == id } ?: BURGERS
    }
}

/** Pontos de carne disponíveis para hambúrgueres. */
enum class MeatPoint(val label: String) {
    RARE("Mal passado"),
    MEDIUM("Ao ponto"),
    WELL_DONE("Bem passado");

    companion object {
        val default: MeatPoint = MEDIUM
        fun fromLabel(label: String): MeatPoint =
            entries.firstOrNull { it.label == label } ?: MEDIUM
    }
}

/** Adicionais do produto com seus respectivos preços fixos. */
enum class Extra(val label: String, val price: Double) {
    BACON("Bacon", 4.0),
    EXTRA_CHEESE("Cheddar extra", 3.5),
    EGG("Ovo", 2.5),
    CARAMELIZED_ONION("Cebola caramelizada", 3.0)
}

/** Status do pedido — usados na timeline de acompanhamento. */
enum class OrderStatus(val label: String, val step: Int) {
    RECEIVED("Pedido recebido", 0),
    PREPARING("Em preparo", 1),
    ON_THE_WAY("Saiu para entrega", 2),
    DELIVERED("Entregue", 3);

    companion object {
        fun fromStep(step: Int): OrderStatus =
            entries.firstOrNull { it.step == step } ?: RECEIVED
    }
}

/** Resumo financeiro do carrinho (calculado no ViewModel). */
data class CartSummary(
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val discount: Double = 0.0,
    val freeShipping: Boolean = false,
    val couponCode: String? = null,
    val itemCount: Int = 0
) {
    /** Total final = subtotal - desconto + entrega. */
    val total: Double get() = (subtotal - discount + deliveryFee).coerceAtLeast(0.0)
}
