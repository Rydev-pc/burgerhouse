package com.burgerhouse.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.burgerhouse.app.BurgerHouseApp
import com.burgerhouse.app.data.repository.BurgerRepository
import com.burgerhouse.app.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Autenticação e sessão do usuário.
 * Faz login/cadastro (dados salvos no Room) e guarda a sessão no DataStore.
 */
class AuthViewModel(
    private val repository: BurgerRepository,
    private val preferences: UserPreferencesRepository
) : ViewModel() {

    /** true quando existe sessão ativa. */
    val sessionActive: StateFlow<Boolean> = preferences.sessionActive
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** true quando o onboarding já foi concluído. */
    val onboardingDone: StateFlow<Boolean> = preferences.onboardingDone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** Dados do usuário logado (prefill dos formulários). */
    val user: StateFlow<com.burgerhouse.app.data.local.entity.User?> = repository.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Cria a conta (ou atualiza os dados) e marca a sessão como ativa.
     * Retornos de validação ficam por conta da UI; aqui persistimos.
     */
    fun login(name: String, email: String, phone: String, address: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.registerUser(name, email, phone, address)
                preferences.setSessionActive(true, email)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            preferences.setSessionActive(false)
        }
    }

    fun finishOnboarding() {
        viewModelScope.launch {
            preferences.finishOnboarding()
        }
    }

    fun consumeError() {
        _error.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as BurgerHouseApp
                AuthViewModel(app.repository, app.preferences)
            }
        }
    }
}
