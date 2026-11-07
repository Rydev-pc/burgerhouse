package com.burgerhouse.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.burgerhouse.app.data.local.entity.Coupon
import kotlinx.coroutines.flow.Flow

/** DAO de cupons de desconto. */
@Dao
interface CouponDao {

    @Query("SELECT * FROM coupons WHERE active = 1 ORDER BY code ASC")
    fun getActiveCoupons(): Flow<List<Coupon>>

    @Query("SELECT * FROM coupons WHERE code = :code LIMIT 1")
    suspend fun findByCode(code: String): Coupon?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(coupon: Coupon)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(coupons: List<Coupon>)

    @Query("DELETE FROM coupons")
    suspend fun deleteAll()
}
