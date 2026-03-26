package com.example.project.data.repository

import com.example.project.data.dao.ProductDao
import com.example.project.data.entities.CartItem
import com.example.project.data.entities.Order
import com.example.project.data.entities.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    // Lấy tất cả sản phẩm (Cho trang chủ)
    fun getAllProducts() = productDao.getAllProducts()

    // Lấy sản phẩm của Shop
    fun getProductsByShop(email: String) = productDao.getProductsByOwnerFlow(email)

    suspend fun insertProduct(product: Product) = productDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    // Giỏ hàng
    fun getCartFlow(userId: Int) = productDao.getCartFlow(userId)
    suspend fun addToCart(item: CartItem) = productDao.addToCart(item)
    suspend fun clearCart(userId: Int) = productDao.clearCart(userId)

    // Đơn hàng
    suspend fun createOrder(order: Order) = productDao.createOrder(order)

    fun getOrdersForShop(email: String) = productDao.getOrdersForShopFlow(email)

    fun getAllOrdersForAdmin() = productDao.getAllOrdersFlow()

    suspend fun updateOrderStatus(orderId: Int, status: String) =
        productDao.updateOrderStatus(orderId, status)

    fun searchProducts(query: String) = productDao.searchProducts(query)
}