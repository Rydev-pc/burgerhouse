package com.burgerhouse.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Usuário logado. O app mantém um único usuário local (id fixo = 1);
 * a sessão ativa é controlada pelo DataStore.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: Long = 1L,
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val loyaltyPoints: Int = 0
)
