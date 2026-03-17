package com.example.project.data.repository

import com.example.project.data.dao.ProductDao
import com.example.project.data.entities.Product
import com.example.project.data.entities.CartItem
import com.example.project.data.entities.Order

class ProductRepository(private val productDao: ProductDao) {
    // Sản phẩm
    suspend fun getAllProducts() = productDao.getAllProducts()
    suspend fun insertProduct(product: Product) = productDao.insertProduct(product)

    // Giỏ hàng
    suspend fun addToCart(item: CartItem) = productDao.addToCart(item)
    suspend fun getCart(userId: Int) = productDao.getCartByUser(userId)
    suspend fun updateCart(id: Int, qty: Int) = productDao.updateCartQuantity(id, qty)

    // Thanh toán
    suspend fun checkout(order: Order) {
        productDao.createOrder( order)
        productDao.clearCart(order.userId) // Thanh toán xong thì xóa giỏ hàng
    }
}