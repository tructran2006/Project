package com.example.project.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_table")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val shopEmail: String,
    val productDescription: String,
    val totalPrice: Double,
    val status: String = "Pending",
    val note: String? = "",
    val timestamp: Long = System.currentTimeMillis()
)