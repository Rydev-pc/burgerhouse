package com.burgerhouse.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade Room que representa um produto do cardápio.
 * A categoria é persistida como String e convertida para o enum
 * [com.burgerhouse.app.domain.model.ProductCategory] na camada de UI.
 */
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val shortDescription: String,
    val description: String,
    val price: Double,
    val category: String,
    val imageUrl: String
)
