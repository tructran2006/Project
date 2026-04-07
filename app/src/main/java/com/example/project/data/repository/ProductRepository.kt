package com.example.project.data.repository

import com.example.project.data.dao.ProductDao
import com.example.project.data.entities.CartItem
import com.example.project.data.entities.Order
import com.example.project.data.entities.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {



    // Lấy sản phẩm của Shop
    fun getProductsByShop(email: String) = productDao.getProductsByOwnerFlow(email)
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    fun getOrdersForShop(email: String) = productDao.getOrdersForShopFlow(email)
    suspend fun updateOrderStatus(orderId: Int, status: String) =
        productDao.updateOrderStatus(orderId, status)
}