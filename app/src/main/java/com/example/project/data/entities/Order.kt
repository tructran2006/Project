package com.example.project.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_table")
data class Order(
    @PrimaryKey(autoGenerate = true)
    val orderId: Int = 0,
    val userId: Int,
    val totalPrice: Double,
    val status: String = "Pending", // Pending, Processing, Shipped, Delivered
    val timestamp: Long = System.currentTimeMillis()
)