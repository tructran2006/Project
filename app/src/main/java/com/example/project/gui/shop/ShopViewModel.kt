package com.example.project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.data.entities.Order
import com.example.project.data.entities.Product
import com.example.project.data.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ShopViewModel(private val repository: ProductRepository) : ViewModel() {

    // Lấy danh sách sản phẩm của riêng shop này
    fun getShopProducts(email: String): Flow<List<Product>> {
        return repository.getProductsByShop(email)
    }

    // Lấy danh sách đơn hàng khách đặt tại shop này
    fun getShopOrders(email: String): Flow<List<Order>> {
        return repository.getOrdersForShop(email)
    }

    // Cập nhật trạng thái đơn hàng (Duyệt đơn, Đang giao...)
    fun updateStatus(orderId: Int, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
        }
    }

    // Xóa sản phẩm
    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }
}