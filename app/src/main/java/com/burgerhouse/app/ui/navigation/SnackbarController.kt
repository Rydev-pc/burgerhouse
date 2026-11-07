package com.burgerhouse.app.ui.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Canal global de mensagens para Snackbars.
 * Qualquer camada (ViewModel/Repository) pode emitir feedback que
 * será exibido pela Snackbar da tela atual.
 */
object SnackbarController {

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val events: SharedFlow<String> = _events.asSharedFlow()

    fun show(message: String) {
        _events.tryEmit(message)
    }
}
