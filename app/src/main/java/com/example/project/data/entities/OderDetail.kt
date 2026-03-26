package com.example.project.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_detail_table")
data class OrderDetail(
    @PrimaryKey(autoGenerate = true) val detailId: Int = 0,
    val orderId: Int,       // Thuộc đơn hàng nào
    val productId: Int,     // ID sản phẩm
    val productName: String, // Lưu tên sản phẩm (phòng trường hợp sau này shop đổi tên)
    val productPrice: Double,// Lưu giá lúc mua (phòng trường hợp sau này shop đổi giá)
    val quantity: Int,      // Số lượng khách mua
    val shopEmail: String   // Để lọc xem sản phẩm này thuộc shop nào
)