package com.burgerhouse.app.ui.util

import java.text.NumberFormat
import java.util.Locale

/** Utilitário de formatação monetária no padrão brasileiro (R$). */
object Money {

    private val format: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    /** Ex.: 29.9 -> "R$ 29,90" */
    fun format(value: Double): String = format.format(value)
}

/** Validações de formulário reutilizadas em Login/Perfil. */
object Validation {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val PHONE_REGEX = Regex("^\\(?\\d{2}\\)?[\\s-]?9?\\d{4}[\\s-]?\\d{4}$")

    fun isValidEmail(email: String): Boolean = email.matches(EMAIL_REGEX)

    fun isValidPhone(phone: String): Boolean =
        phone.filter { it.isDigit() }.length >= 10 && phone.matches(PHONE_REGEX)

    fun isNotBlank(text: String): Boolean = text.isNotBlank()
}
