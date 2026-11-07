package com.burgerhouse.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.burgerhouse.app.data.local.entity.CartItem
import kotlinx.coroutines.flow.Flow

/** DAO do carrinho de compras. */
@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items ORDER BY id ASC")
    fun getCartItems(): Flow<List<CartItem>>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM cart_items")
    fun getTotalItemCount(): Flow<Int>

    @Insert
    suspend fun insert(item: CartItem): Long

    @Update
    suspend fun update(item: CartItem)

    @Delete
    suspend fun delete(item: CartItem)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM cart_items")
    suspend fun clear()
}
