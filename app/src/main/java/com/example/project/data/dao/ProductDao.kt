package com.example.project.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.project.data.entities.CartItem
import com.example.project.data.entities.Category
import com.example.project.data.entities.Order
import com.example.project.data.entities.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM product_table")
    fun getAllProducts(): Flow<List<Product>>


    @Query("SELECT * FROM product_table WHERE ownerEmail = :email")
    fun getProductsByOwnerFlow(email: String): Flow<List<Product>>

    // --- Lệnh cho Danh mục ---
    @Update
    suspend fun updateCategory(category: Category)


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


    // --- Lệnh cho Đơn hàng ---
    @Insert
    suspend fun createOrder(order: Order)

    // Lấy đơn hàng theo User (Người mua xem lịch sử)
    @Query("SELECT * FROM order_table WHERE userId = :uId ORDER BY id DESC")
    suspend fun getOrdersByUser(uId: Int): List<Order>

    // THÊM: Lấy đơn hàng theo Shop (Admin quản lý đơn hàng của khách mua tại shop mình)
    @Query("SELECT * FROM order_table WHERE id IN (SELECT id FROM order_table) ORDER BY id DESC")
    fun getAllOrdersFlow(): Flow<List<Order>>


    @Query("UPDATE product_table SET category = :newName WHERE category = :oldName")
    suspend fun updateProductCategoryNames(oldName: String, newName: String)

    @Query("UPDATE product_table SET category = 'Chưa phân loại' WHERE category = :deletedName")
    suspend fun resetProductCategoryAfterDelete(deletedName: String)
}