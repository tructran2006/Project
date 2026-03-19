package com.example.project.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.project.data.entities.Order

@Dao
interface OrderDao {
    @Query("SELECT * FROM order_table")
    fun getAllOrders(): kotlinx.coroutines.flow.Flow<List<Order>>

    @Query("UPDATE order_table SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Int, status: String)
}