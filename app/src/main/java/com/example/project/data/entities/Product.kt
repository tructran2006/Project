package com.example.project.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_table")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val price: Double,
    val description: String,
    val imageUrl: String, // Lưu đường dẫn ảnh
    val ownerId: Int // ID của Admin/Shop tạo ra sản phẩm này
)