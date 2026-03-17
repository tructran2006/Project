package com.example.project.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_table")
data class CartItem(
    @PrimaryKey(autoGenerate = true)
    val cartId: Int = 0,
    val userId: Int,    // Sản phẩm này của User nào
    val productId: Int, // ID sản phẩm khách chọn
    val quantity: Int   // Số lượng mua
)