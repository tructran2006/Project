package com.example.project.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_table")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val shopEmail: String,        // Kiểm tra xem đã có dòng này chưa
    val productDescription: String, // Kiểm tra xem đã có dòng này chưa
    val totalPrice: Double,
    val status: String = "Pending",
    val note: String? = "",         // Kiểm tra xem đã có dòng này chưa
    val timestamp: Long = System.currentTimeMillis()
)