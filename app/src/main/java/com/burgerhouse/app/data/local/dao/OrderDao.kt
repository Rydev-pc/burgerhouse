package com.burgerhouse.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.burgerhouse.app.data.local.entity.Order
import com.burgerhouse.app.data.local.entity.OrderItem
import com.burgerhouse.app.data.local.entity.OrderWithItems
import kotlinx.coroutines.flow.Flow

/** DAO de pedidos e itens de pedido. */
@Dao
interface OrderDao {

    @Insert
    suspend fun insertOrder(order: Order): Long

    @Insert
    suspend fun insertItems(items: List<OrderItem>)

    @Update
    suspend fun updateOrder(order: Order)

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrder(orderId: Long): Flow<Order?>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrderWithItems(orderId: Long): Flow<OrderWithItems?>

    @Transaction
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrdersWithItems(): Flow<List<OrderWithItems>>

    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteOrder(orderId: Long)
}
