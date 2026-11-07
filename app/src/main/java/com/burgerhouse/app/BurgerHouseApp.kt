package com.burgerhouse.app

import android.app.Application
import com.burgerhouse.app.data.local.database.BurgerHouseDatabase
import com.burgerhouse.app.data.repository.BurgerRepository
import com.burgerhouse.app.data.repository.UserPreferencesRepository

/**
 * Application class do app. Atua como um ServiceLocator simples,
 * expondo as dependências principais para os ViewModels.
 */
class BurgerHouseApp : Application() {

    /** Banco Room (singleton). */
    val database: BurgerHouseDatabase by lazy { BurgerHouseDatabase.getInstance(this) }

    /** Repositório principal (produtos, carrinho, pedidos, cupons). */
    val repository: BurgerRepository by lazy {
        BurgerRepository(
            productDao = database.productDao(),
            cartDao = database.cartDao(),
            orderDao = database.orderDao(),
            userDao = database.userDao(),
            couponDao = database.couponDao()
        )
    }

    /** Repositório de preferências/sessão (DataStore). */
    val preferences: UserPreferencesRepository by lazy { UserPreferencesRepository(this) }
}
