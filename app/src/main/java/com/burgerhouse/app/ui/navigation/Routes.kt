package com.burgerhouse.app.ui.navigation

/** Rotas de navegação do app. */
object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val HOME = "home"
    const val PRODUCT_DETAIL = "product/{productId}"
    const val CART = "cart"
    const val CHECKOUT = "checkout"
    const val TRACKING = "tracking/{orderId}"
    const val HISTORY = "history"
    const val LOYALTY = "loyalty"
    const val PROFILE = "profile"

    /** Rota do detalhe do produto com o ID preenchido. */
    fun productDetail(productId: Long) = "product/$productId"

    /** Rota do acompanhamento com o ID do pedido preenchido. */
    fun tracking(orderId: Long) = "tracking/$orderId"
}

/** Itens da bottom bar. */
enum class BottomItem(val route: String, val label: String, val emoji: String) {
    HOME(Routes.HOME, "Home", "🍔"),
    HISTORY(Routes.HISTORY, "Pedidos", "🧾"),
    LOYALTY(Routes.LOYALTY, "Fidelidade", "🎁"),
    PROFILE(Routes.PROFILE, "Perfil", "👤");

    companion object {
        val routes: List<String> = entries.map { it.route }
    }
}
