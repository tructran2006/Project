package com.example.project.data.dao

import androidx.room.*
import com.example.project.data.entities.CartItem
import com.example.project.data.entities.Category
import com.example.project.data.entities.Order
import com.example.project.data.entities.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    // ========================================================================
    // 1. QUẢN LÝ SẢN PHẨM (PRODUCT)
    // ========================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM product_table")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM product_table")
    suspend fun getAllProductsList(): List<Product>

    @Query("SELECT * FROM product_table WHERE ownerEmail = :email")
    fun getProductsByOwnerFlow(email: String): Flow<List<Product>>

    @Query("SELECT * FROM product_table WHERE id = :pId LIMIT 1")
    suspend fun getProductById(pId: Int): Product?

    // Thống kê tổng doanh thu (Chỉ tính đơn đã hoàn thành)
    @Query("SELECT SUM(totalPrice) FROM order_table WHERE status = 'Completed'")
    fun getTotalRevenue(): kotlinx.coroutines.flow.Flow<Double?>

    // Thống kê số lượng đơn hàng đang chờ xử lý
    @Query("SELECT COUNT(id) FROM order_table WHERE status = 'Pending'")
    fun getPendingOrderCount(): kotlinx.coroutines.flow.Flow<Int>

    // Thống kê tổng số sản phẩm trong hệ thống
    @Query("SELECT COUNT(id) FROM product_table")
    fun getTotalProductCount(): kotlinx.coroutines.flow.Flow<Int>
    // ========================================================================
    // 2. QUẢN LÝ GIỎ HÀNG (CART)
    // ========================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(cartItem: CartItem)

    @Delete
    suspend fun deleteCartItem(cartItem: CartItem)

    @Query("SELECT * FROM cart_table WHERE userId = :uId")
    suspend fun getCartByUser(uId: Int): List<CartItem>

    @Query("UPDATE cart_table SET quantity = :newQty WHERE cartId = :cartItemId")
    suspend fun updateCartQuantity(cartItemId: Int, newQty: Int)

    @Query("SELECT * FROM cart_table WHERE userId = :uId")
    fun getCartFlow(uId: Int): Flow<List<CartItem>>
    @Query("DELETE FROM cart_table WHERE userId = :uId")
    suspend fun clearCart(uId: Int)

    @Query("SELECT * FROM cart_table WHERE userId = :userId AND productId = :productId LIMIT 1")
    suspend fun getCartItem(userId: Int, productId: Int): CartItem?


    // ========================================================================
    // 3. QUẢN LÝ ĐƠN HÀNG (ORDER)
    // ========================================================================

    @Insert
    suspend fun createOrder(order: Order)

    // Người mua xem đơn hàng (Flow để cập nhật UI tự động)
    @Query("SELECT * FROM order_table WHERE userId = :uId ORDER BY id DESC")
    fun getOrdersByUserFlow(uId: Int): Flow<List<Order>>

    // Shop xem đơn hàng khách đặt
    @Query("SELECT * FROM order_table WHERE shopEmail = :email ORDER BY id DESC")
    fun getOrdersForShopFlow(email: String): Flow<List<Order>>

    // Admin xem toàn bộ hệ thống
    @Query("SELECT * FROM order_table ORDER BY id DESC")
    fun getAllOrdersFlow(): Flow<List<Order>>

    @Query("UPDATE order_table SET status = :newStatus WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Int, newStatus: String)


    // ========================================================================
    // 4. QUẢN LÝ DANH MỤC (CATEGORY)
    // ========================================================================

    @Update
    suspend fun updateCategory(category: Category)

    @Query("UPDATE product_table SET category = :newName WHERE category = :oldName")
    suspend fun updateProductCategoryNames(oldName: String, newName: String)

    @Query("UPDATE product_table SET category = 'Chưa phân loại' WHERE category = :deletedName")
    suspend fun resetProductCategoryAfterDelete(deletedName: String)

    @Query("SELECT * FROM product_table WHERE name LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<Product>>

    // quản lý shop


    // Đếm số lượng email chủ cửa hàng duy nhất (để biết có bao nhiêu Shop)
    // Đếm chính xác số người dùng có vai trò là người bán (Shop)
    @Query("SELECT COUNT(id) FROM user_table WHERE role = 'shop'")
    fun getTotalShopCount(): kotlinx.coroutines.flow.Flow<Int>

    // Đếm toàn bộ sản phẩm không phân biệt chủ sở hữu
    @Query("SELECT COUNT(id) FROM product_table")
    fun getSystemProductCount(): kotlinx.coroutines.flow.Flow<Int>
}