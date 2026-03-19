package com.example.project.gui.shop

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project.MainActivity
import com.example.project.data.database.AppDatabase
import com.example.project.data.entities.Category
import com.example.project.data.entities.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.emptyList


class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AdminScreen()
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Sản phẩm", "Danh mục", "Đơn hàng")

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Trang Quản Trị", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { (context as? Activity)?.finish() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        }
                    }
                )
                // Thanh chuyển Tab
                TabRow(selectedTabIndex = selectedTab, containerColor = Color.White) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> ProductManager(db)  // Quản lý sản phẩm
                1 -> CategoryManager(db) // Quản lý danh mục
                2 -> OrderManager(db)    // Quản lý đơn hàng
            }
        }
    }
}

@Composable
fun ProductManager(db: AppDatabase) {
    val productList by db.productDao().getAllProducts().collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(productList) { product ->
                AdminProductItem(product, onDelete = {
                    CoroutineScope(Dispatchers.IO).launch { db.productDao().deleteProduct(product) }
                })
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = Color(0xFFF48C25)
        ) { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) }
    }

    if (showDialog) {
        AddProductWithImageDialog(
            db = db,
            onDismiss = { showDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductWithImageDialog(db: AppDatabase, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imgUrl by remember { mutableStateOf("") }

    val categoryList by db.categoryDao().getAllCategories().collectAsState(initial = emptyList())
    var selectedCategory by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(categoryList) {
        if (categoryList.isNotEmpty() && selectedCategory.isEmpty()) {
            selectedCategory = categoryList[0].name
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm sản phẩm mới", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên sản phẩm") }, modifier = Modifier.fillMaxWidth())

                OutlinedTextField(
                    value = price,
                    onValueChange = { if (it.all { char -> char.isDigit() }) price = it },
                    label = { Text("Giá tiền (VNĐ)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ví dụ: 500000") }
                )

                // Dropdown Menu
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = if (selectedCategory.isEmpty()) "Chọn danh mục" else selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Danh mục") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            categoryList.forEach { cat ->
                                DropdownMenuItem(text = { Text(cat.name) }, onClick = { selectedCategory = cat.name; expanded = false })
                            }
                        }
                    }
                }

                OutlinedTextField(value = imgUrl, onValueChange = { imgUrl = it }, label = { Text("Link ảnh (URL)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceDouble = price.toDoubleOrNull() ?: -1.0

                    // ĐIỀU KIỆN LƯU
                    if (name.isBlank() || price.isBlank() || selectedCategory.isBlank() || imgUrl.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập đầy đủ tất cả thông tin!", Toast.LENGTH_SHORT).show()
                    } else if (priceDouble < 0) {
                        Toast.makeText(context, "Giá tiền không hợp lệ!", Toast.LENGTH_SHORT).show()
                    } else if (priceDouble > 100_000_000_000.0) { // GIỚI HẠN 100 TỶ
                        Toast.makeText(context, "Giá tiền không được vượt quá 100 tỷ VNĐ!", Toast.LENGTH_SHORT).show()
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            val p = Product(
                                name = name,
                                price = priceDouble,
                                category = selectedCategory,
                                imageUrl = imgUrl
                            )
                            db.productDao().insertProduct(p)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Đã thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48C25))
            ) { Text("Lưu sản phẩm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Composable
fun CategoryManager(db: AppDatabase) {
    val categoryList by db.categoryDao().getAllCategories().collectAsState(initial = emptyList())
    var newCatName by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("Quản lý danh mục", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newCatName,
                onValueChange = { newCatName = it },
                label = { Text("Tên danh mục mới") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (newCatName.isNotBlank()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            db.categoryDao().insertCategory(Category(name = newCatName.trim()))
                            withContext(Dispatchers.Main) {
                                newCatName = ""
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48C25))
            ) { Text("Thêm") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Danh sách đã có:", fontSize = 14.sp, color = Color.Gray)

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(categoryList) { cat ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Text(
                        cat.name,
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
@Composable
fun OrderManager(db: AppDatabase) {
    // Giả sử bạn đã có bảng Order trong Database
    val orderList by db.orderDao().getAllOrders().collectAsState(initial = emptyList())

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(orderList) { order ->
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Đơn hàng #${order.id}", fontWeight = FontWeight.Bold)
                    Text("Tổng tiền: ${order.totalPrice}đ")
                    Text("Trạng thái hiện tại: ${order.status}", color = Color(0xFFF48C25))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            updateStatus(db, order.id, "Đang giao")
                        }) { Text("Giao hàng", fontSize = 10.sp) }

                        Button(onClick = {
                            updateStatus(db, order.id, "Hoàn thành")
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color.Green)) {
                            Text("Hoàn thành", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

fun updateStatus(db: AppDatabase, orderId: Int, newStatus: String) {
    CoroutineScope(Dispatchers.IO).launch {
        db.orderDao().updateOrderStatus(orderId, newStatus)
    }
}
@Composable
fun AdminProductItem(product: Product, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hiển thị ảnh (nếu bạn dùng URL ảnh)
            Box(modifier = Modifier.size(50.dp).background(Color(0xFFE0F2FE), RoundedCornerShape(8.dp)))

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold)
                Text("${product.price}đ", color = Color(0xFFF48C25))
                Text(product.category, fontSize = 11.sp, color = Color.Gray)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
            }
        }
    }
}