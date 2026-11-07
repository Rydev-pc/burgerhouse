package com.burgerhouse.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.burgerhouse.app.BurgerHouseApp
import com.burgerhouse.app.data.local.entity.User
import com.burgerhouse.app.data.repository.BurgerRepository
import com.burgerhouse.app.data.repository.UserPreferencesRepository
import com.burgerhouse.app.ui.navigation.SnackbarController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Perfil do usuário: dados editáveis, endereços, configurações
 * (notificações e tema) e logout.
 */
class ProfileViewModel(
    private val repository: BurgerRepository,
    private val preferences: UserPreferencesRepository
) : ViewModel() {

    val user: StateFlow<User?> = repository.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /* Campos do formulário (preenchidos a partir do usuário) */
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address.asStateFlow()

    /** Configurações */
    val notificationsEnabled: StateFlow<Boolean> = preferences.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val darkMode: StateFlow<Boolean> = preferences.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val logoutDone = MutableStateFlow(false)

    init {
        // Pré-preenche os campos quando o usuário estiver disponível
        viewModelScope.launch {
            repository.getUser().collect { current ->
                if (current != null && _name.value.isBlank()) {
                    _name.value = current.name
                    _email.value = current.email
                    _phone.value = current.phone
                    _address.value = current.address
                }
            }
        }
    }

    fun onNameChange(value: String) { _name.value = value }
    fun onEmailChange(value: String) { _email.value = value }
    fun onPhoneChange(value: String) { _phone.value = value }
    fun onAddressChange(value: String) { _address.value = value }

    /** Salva as alterações do perfil. */
    fun saveProfile() {
        viewModelScope.launch {
            val current = repository.getUser().first()
            val updated = current?.copy(
                name = _name.value.trim(),
                email = _email.value.trim(),
                phone = _phone.value.trim(),
                address = _address.value.trim()
            ) ?: User(
                name = _name.value.trim(),
                email = _email.value.trim(),
                phone = _phone.value.trim(),
                address = _address.value.trim()
            )
            repository.saveUser(updated)
            SnackbarController.show("Perfil atualizado com sucesso")
        }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch { preferences.setNotifications(enabled) }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { preferences.setDarkMode(enabled) }
    }

    fun logout() {
        viewModelScope.launch {
            preferences.setSessionActive(false)
            logoutDone.value = true
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as BurgerHouseApp
                ProfileViewModel(app.repository, app.preferences)
            }
        }
    }
}
