package com.burgerhouse.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cupom de desconto. Tipos suportados:
 *  - "PERCENT": [value] representa a porcentagem de desconto (ex.: 10.0)
 *  - "FREESHIP": isenta a taxa de entrega
 */
@Entity(tableName = "coupons")
data class Coupon(
    @PrimaryKey
    val code: String,
    val description: String,
    val type: String,
    val value: Double,
    val active: Boolean = true
) {
    companion object {
        const val TYPE_PERCENT = "PERCENT"
        const val TYPE_FREESHIP = "FREESHIP"
    }
}
