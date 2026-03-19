package com.example.project.data.dao

import androidx.room.*
import com.example.project.data.entities.Product
import com.example.project.data.entities.CartItem
import com.example.project.data.entities.Order
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    // --- Lệnh cho Sản phẩm ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM product_table")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM product_table WHERE ownerId = :shopId")
    suspend fun getProductsByShop(shopId: Int): List<Product>

    // --- Lệnh cho Giỏ hàng ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(cartItem: CartItem)

    @Query("SELECT * FROM cart_table WHERE userId = :uId")
    suspend fun getCartByUser(uId: Int): List<CartItem>

    @Delete
    suspend fun removeFromCart(cartItem: CartItem)

    @Query("UPDATE cart_table SET quantity = :qty WHERE cartId = :id")
    suspend fun updateCartQuantity(id: Int, qty: Int)

    @Query("DELETE FROM cart_table WHERE userId = :uId")
    suspend fun clearCart(uId: Int)

    // Lệnh cho Đơn hàng
    @Insert
    suspend fun createOrder(order: Order)

    @Query("SELECT * FROM order_table WHERE userId = :uId ORDER BY timestamp DESC")
    suspend fun getOrdersByUser(uId: Int): List<Order>
}
