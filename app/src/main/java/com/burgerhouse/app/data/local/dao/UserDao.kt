package com.burgerhouse.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.burgerhouse.app.data.local.entity.User
import kotlinx.coroutines.flow.Flow

/** DAO de usuário (registro único local). */
@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    fun getUser(): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: User)

    @Update
    suspend fun update(user: User)

    @Query("UPDATE users SET loyaltyPoints = :points WHERE id = 1")
    suspend fun updatePoints(points: Int)

    @Query("DELETE FROM users")
    suspend fun clear()
}
