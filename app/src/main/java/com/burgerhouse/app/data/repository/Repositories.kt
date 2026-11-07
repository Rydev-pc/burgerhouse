package com.burgerhouse.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "burger_house_prefs"
)

/**
 * Repositório de sessão e preferências do app (DataStore Preferences).
 * Controla: sessão ativa, onboarding concluído, tema escuro, notificações.
 */
class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val SESSION_ACTIVE = booleanPreferencesKey("session_active")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val LAST_USER_EMAIL = stringPreferencesKey("last_user_email")
    }

    val sessionActive: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.SESSION_ACTIVE] ?: false }

    val onboardingDone: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }

    val darkMode: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.DARK_MODE] ?: false }

    val notificationsEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.NOTIFICATIONS] ?: true }

    suspend fun finishOnboarding() {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = true }
    }

    suspend fun setSessionActive(active: Boolean, email: String = "") {
        context.dataStore.edit {
            it[Keys.SESSION_ACTIVE] = active
            if (email.isNotBlank()) it[Keys.LAST_USER_EMAIL] = email
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setNotifications(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS] = enabled }
    }

    suspend fun isLoggedIn(): Boolean = sessionActive.first()
}

/**
 * Repositório principal do app (camada Repository do MVVM):
 * produtos, carrinho, pedidos, cupons e usuário do Room.
 */
class BurgerRepository(
    private val productDao: ProductDao,
    private val cartDao: CartDao,
    private val orderDao: OrderDao,
    private val userDao: UserDao,
    private val couponDao: CouponDao
) {

    // ---------- Produtos ----------

    fun getProducts(): Flow<List<Product>> = productDao.getAll()

    fun getProductsByCategory(category: String): Flow<List<Product>> =
        productDao.getByCategory(category)

    fun searchProducts(query: String): Flow<List<Product>> = productDao.search(query)

    fun getProductById(id: Long): Flow<Product?> = productDao.getById(id)

    suspend fun insertProduct(product: Product) {
        productDao.insert(product)
    }

    // ---------- Carrinho ----------

    fun getCartItems(): Flow<List<CartItem>> = cartDao.getCartItems()

    fun getCartItemCount(): Flow<Int> = cartDao.getTotalItemCount()

    suspend fun addToCart(item: CartItem) {
        cartDao.insert(item)
    }

    suspend fun updateCartItem(item: CartItem) {
        cartDao.update(item)
    }

    suspend fun removeFromCart(item: CartItem) {
        cartDao.delete(item)
    }

    suspend fun clearCart() {
        cartDao.clear()
    }

    // ---------- Pedidos ----------

    fun getOrders(): Flow<List<Order>> = orderDao.getAllOrders()

    fun getOrdersWithItems(): Flow<List<com.burgerhouse.app.data.local.entity.OrderWithItems>> =
        orderDao.getAllOrdersWithItems()

    fun getOrderWithItems(orderId: Long): Flow<com.burgerhouse.app.data.local.entity.OrderWithItems?> =
        orderDao.getOrderWithItems(orderId)

    suspend fun placeOrder(
        order: Order,
        items: List<OrderItem>
    ): Long {
        val orderId = orderDao.insertOrder(order)
        orderDao.insertItems(items.map { it.copy(orderId = orderId) })
        cartDao.clear()
        return orderId
    }

    suspend fun updateOrder(order: Order) {
        orderDao.updateOrder(order)
    }

    suspend fun deleteOrder(orderId: Long) {
        orderDao.deleteOrder(orderId)
    }

    // ---------- Usuário ----------

    fun getUser(): Flow<User?> = userDao.getUser()

    suspend fun saveUser(user: User) {
        userDao.upsert(user)
    }

    suspend fun addLoyaltyPoints(points: Int) {
        val current = userDao.getUser().first()
        userDao.updatePoints((current?.loyaltyPoints ?: 0) + points)
    }

    suspend fun registerUser(name: String, email: String, phone: String, address: String) {
        val existing = userDao.getUser().first()
        userDao.upsert(
            User(
                id = 1L,
                name = name.trim(),
                email = email.trim(),
                phone = phone.trim(),
                address = address.trim(),
                loyaltyPoints = existing?.loyaltyPoints ?: 0
            )
        )
    }

    suspend fun updateUserAddress(address: String) {
        val current = userDao.getUser().first() ?: return
        userDao.upsert(current.copy(address = address.trim()))
    }

    // ---------- Cupons ----------

    fun getActiveCoupons(): Flow<List<Coupon>> = couponDao.getActiveCoupons()

    suspend fun findCoupon(code: String): Coupon? = couponDao.findByCode(code.uppercase().trim())

    suspend fun insertCoupon(coupon: Coupon) {
        couponDao.insert(coupon)
    }
}

/**
 * Aplica o seed na primeira execução: insere os produtos do cardápio
 * e os cupons disponíveis. Deve ser chamado em IO durante o startup.
 */
suspend fun seedDatabaseIfEmpty(repository: BurgerRepository) {
    withContext(Dispatchers.IO) {
        val needsSeed = repository.getProducts().first().isEmpty()
        if (needsSeed) {
            com.burgerhouse.app.data.local.database.DatabaseSeeder.products.forEach {
                repository.insertProduct(it)
            }
            com.burgerhouse.app.data.local.database.DatabaseSeeder.coupons.forEach {
                repository.insertCoupon(it)
            }
        }
    }
}
