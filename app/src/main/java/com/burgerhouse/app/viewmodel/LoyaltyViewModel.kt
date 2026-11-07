package com.burgerhouse.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.burgerhouse.app.BurgerHouseApp
import com.burgerhouse.app.data.local.entity.Coupon
import com.burgerhouse.app.data.local.entity.User
import com.burgerhouse.app.data.repository.BurgerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Programa de fidelidade: pontos acumulados, progresso até a
 * recompensa (10 pontos = 1 hambúrguer grátis) e cupons disponíveis.
 */
class LoyaltyViewModel(
    private val repository: BurgerRepository
) : ViewModel() {

    companion object {
        /** Pontos necessários para ganhar um hambúrguer grátis. */
        const val POINTS_PER_REWARD = 10

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as BurgerHouseApp
                LoyaltyViewModel(app.repository)
            }
        }
    }

    /** Estado consolidado da tela de fidelidade. */
    val loyaltyState: StateFlow<LoyaltyUiState> = combine(
        repository.getUser(),
        repository.getActiveCoupons()
    ) { user, coupons ->
        val points = user?.loyaltyPoints ?: 0
        LoyaltyUiState(
            points = points,
            burgersEarned = points / POINTS_PER_REWARD,
            progressToReward = (points % POINTS_PER_REWARD) / POINTS_PER_REWARD.toFloat(),
            coupons = coupons
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        LoyaltyUiState()
    )

    /** Dados do usuário (nome exibido no cartão de pontos). */
    val user: StateFlow<User?> = repository.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

/** Estado da UI de fidelidade. */
data class LoyaltyUiState(
    val points: Int = 0,
    val burgersEarned: Int = 0,
    val progressToReward: Float = 0f,
    val coupons: List<Coupon> = emptyList()
)
