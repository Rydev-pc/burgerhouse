package com.burgerhouse.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.burgerhouse.app.data.local.dao.CartDao
import com.burgerhouse.app.data.local.dao.CouponDao
import com.burgerhouse.app.data.local.dao.OrderDao
import com.burgerhouse.app.data.local.dao.ProductDao
import com.burgerhouse.app.data.local.dao.UserDao
import com.burgerhouse.app.data.local.entity.CartItem
import com.burgerhouse.app.data.local.entity.Coupon
import com.burgerhouse.app.data.local.entity.Order
import com.burgerhouse.app.data.local.entity.OrderItem
import com.burgerhouse.app.data.local.entity.Product
import com.burgerhouse.app.data.local.entity.User

/**
 * Banco de dados Room do app. Singleton thread-safe.
 * O seed inicial é executado pelo [DatabaseSeeder] na primeira abertura.
 */
@Database(
    entities = [
        Product::class,
        CartItem::class,
        Order::class,
        OrderItem::class,
        User::class,
        Coupon::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BurgerHouseDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun userDao(): UserDao
    abstract fun couponDao(): CouponDao

    companion object {
        @Volatile
        private var INSTANCE: BurgerHouseDatabase? = null

        fun getInstance(context: Context): BurgerHouseDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BurgerHouseDatabase::class.java,
                    "burger_house.db"
                )
                    // Permite recrear o banco em caso de schema corrompido
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
