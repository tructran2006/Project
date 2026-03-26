package com.example.project.gui.user

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project.data.database.AppDatabase
import com.example.project.formatPrice
import kotlin.collections.emptyList

@Composable
fun MyOrdersScreen(db: AppDatabase, userEmail: String, onBack: () -> Unit) {
    // Lấy ID người dùng (giả sử là 1 hoặc bạn lấy từ userEmail)
    val orderList by db.productDao().getOrdersByUserFlow(1).collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F7F5))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
            Text("Đơn hàng của tôi", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        if (orderList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Bạn chưa có đơn hàng nào", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(orderList) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Mã đơn: #${order.id}", fontWeight = FontWeight.Bold)
                                Text(order.status, color = if(order.status == "Pending") Color.Red else Color.Green)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Sản phẩm: ${order.productDescription}", fontSize = 14.sp)
                            Text(
                                "Tổng thanh toán: ${formatPrice(order.totalPrice)}",
                                color = Color(0xFFF48C25),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}