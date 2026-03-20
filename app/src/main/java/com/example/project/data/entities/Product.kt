package com.example.project.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_table")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val category: String = "Khác",
    val description: String = "Sản phẩm chất lượng cao",
    val imageUrl: String = "",
    val ownerId: Int = 0,
    val ownerEmail: String

)