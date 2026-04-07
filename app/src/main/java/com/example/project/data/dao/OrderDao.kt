package com.example.project.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.project.data.entities.Order
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    // Tạo đơn hàng mới
    @Insert
    suspend fun createOrder(order: Order): Long

    // --- DÀNH CHO ADMIN ---
    // Lấy toàn bộ đơn hàng của tất cả các shop trên hệ thống
    @Query("SELECT * FROM order_table ORDER BY timestamp DESC")
    fun getAllOrdersFlow(): Flow<List<Order>>

    // --- DÀNH CHO SHOP ---
    // Shop chỉ xem những đơn hàng có sản phẩm thuộc shop mình (lọc theo shopEmail)
    @Query("SELECT * FROM order_table WHERE shopEmail = :email ORDER BY timestamp DESC")
    fun getOrdersForShop(email: String): Flow<List<Order>>

    // --- DÀNH CHO USER (NGƯỜI MUA) ---
    // Xem lịch sử mua hàng cá nhân
    @Query("SELECT * FROM order_table WHERE userId = :uId ORDER BY timestamp DESC")
    fun getOrdersByUser(uId: Int): Flow<List<Order>>

    // Cập nhật trạng thái đơn hàng
    @Query("UPDATE order_table SET status = :newStatus WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Int, newStatus: String)

    // Xóa đơn hàng
    @Query("DELETE FROM order_table WHERE id = :orderId")
    suspend fun deleteOrder(orderId: Int)
}