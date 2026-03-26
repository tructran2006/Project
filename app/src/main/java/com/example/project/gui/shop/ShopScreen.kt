package com.example.project.gui.shop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project.ui.viewmodel.ShopViewModel

@Composable
fun ShopDashboardScreen(shopEmail: String, viewModel: ShopViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Sản phẩm", "Đơn hàng")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> ShopProductList(shopEmail, viewModel)
            1 -> ShopOrderList(shopEmail, viewModel)
        }
    }
}

@Composable
fun ShopOrderList(shopEmail: String, viewModel: ShopViewModel) {
    val orders by viewModel.getShopOrders(shopEmail).collectAsState(initial = emptyList())

    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Chưa có đơn hàng nào")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            items(orders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Đơn hàng #${order.id}", fontWeight = FontWeight.Bold)
                        Text("Mô tả: ${order.productDescription}", color = Color.Gray)
                        Text("Tổng: ${order.totalPrice.toInt()}đ", fontWeight = FontWeight.Bold)
                        Text("Trạng thái: ${order.status}", color = Color.Blue)

                        if (!order.note.isNullOrBlank()) {
                            Text("Ghi chú: ${order.note}", style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Nút xác nhận đơn
                        Button(
                            onClick = { viewModel.updateStatus(order.id, "Delivered") },
                            enabled = order.status != "Delivered",
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(if (order.status == "Delivered") "Đã giao" else "Xác nhận giao")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShopProductList(shopEmail: String, viewModel: ShopViewModel) {
    val products by viewModel.getShopProducts(shopEmail).collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) } // State để mở Dialog thêm sản phẩm

    Box(modifier = Modifier.fillMaxSize()) {
        if (products.isEmpty()) {
            Text("Bạn chưa có sản phẩm nào", modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                items(products) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                                // THÊM PHẦN MÔ TẢ SẢN PHẨM Ở ĐÂY
                                Text(
                                    text = product.description ?: "Không có mô tả",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    maxLines = 2 // Tránh bị quá dài làm xấu giao diện
                                )

                                Text(
                                    text = "${String.format("%,.0f", product.price)}đ",
                                    color = Color(0xFFF48C25),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = { viewModel.deleteProduct(product) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true }, // Mở Dialog khi nhấn nút
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = Color(0xFFF48C25)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
        }
    }

    // Hiển thị Dialog thêm sản phẩm (Xem phần 2 phía dưới)
    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, price, desc ->
                // Gọi ViewModel để lưu vào Database
                // viewModel.addProduct(name, price, desc, shopEmail)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddProductDialog(onDismiss: () -> Unit, onConfirm: (String, Double, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm sản phẩm mới", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên sản phẩm") },
                    modifier = Modifier.fillMaxWidth()
                )
                androidx.compose.material3.OutlinedTextField(
                    value = price,
                    onValueChange = { if (it.all { char -> char.isDigit() }) price = it },
                    label = { Text("Giá tiền") },
                    modifier = Modifier.fillMaxWidth()
                )
                // Ô NHẬP MÔ TẢ (Cho phép nhập nhiều dòng)
                androidx.compose.material3.OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả chi tiết") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, price.toDoubleOrNull() ?: 0.0, description) },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFF48C25))
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}