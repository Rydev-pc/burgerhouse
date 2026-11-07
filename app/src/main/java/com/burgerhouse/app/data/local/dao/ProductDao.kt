package com.burgerhouse.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.burgerhouse.app.data.local.entity.Product
import kotlinx.coroutines.flow.Flow

/** DAO de produtos do cardápio. */
@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY id ASC")
    fun getAll(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY id ASC")
    fun getByCategory(category: String): Flow<List<Product>>

    /** Busca por nome/descrição (usada pelo campo de busca com debounce). */
    @Query(
        "SELECT * FROM products WHERE name LIKE '%' || :query || '%' " +
            "OR description LIKE '%' || :query || '%' ORDER BY id ASC"
    )
    fun search(query: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getById(id: Long): Flow<Product?>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}
