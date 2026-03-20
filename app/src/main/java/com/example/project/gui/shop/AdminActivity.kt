package com.example.project.gui.shop

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    var selectedTab by remember { mutableIntStateOf(0) }

    // Lấy Activity để lấy Email từ Intent và xử lý nút Back
    val activity = context.findActivity()
    val adminEmail = activity?.intent?.getStringExtra("ADMIN_EMAIL") ?: ""
    val tabs = listOf("Sản phẩm", "Danh mục", "Đơn hàng")

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Trang Quản Trị", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = {
                            val intent = Intent(context, com.example.project.gui.auth.LoginActivity::class.java)

                            intent.putExtra("USER_EMAIL", adminEmail)

                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            context.startActivity(intent)
                            activity?.finish()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                        }
                    }
                )
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
                // TRUYỀN adminEmail vào các Manager
                0 -> ProductManager(db, adminEmail)
                1 -> CategoryManager(db)
                2 -> OrderManager(db)
            }
        }
    }
}

@Composable
fun ProductManager(db: AppDatabase, adminEmail: String) {
    // 1. Gọi đúng hàm lọc theo Email đã sửa ở Dao trên
    val productList by db.productDao().getProductsByOwnerFlow(adminEmail).collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }

    // 2. CHỈ ĐỊNH RÕ KIỂU DỮ LIỆU <Product?> ĐỂ HẾT LỖI "Cannot infer type"
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(productList) { product ->
                AdminProductItem(
                    product = product,
                    onEdit = {
                        // Gán nguyên đối tượng product vào state
                        editingProduct = product
                    },
                    onDelete = {
                        CoroutineScope(Dispatchers.IO).launch { db.productDao().deleteProduct(product) }
                    }
                )
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = Color(0xFFF48C25)
        ) { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) }
    }

    // Dialog Thêm
    if (showAddDialog) {
        AddProductWithImageDialog(db = db, adminEmail = adminEmail, onDismiss = { showAddDialog = false })
    }

    // Dialog Sửa (Chỉ hiện khi editingProduct khác null)
    editingProduct?.let { product ->
        EditProductDialog(
            db = db,
            product = product,
            onDismiss = { editingProduct = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductWithImageDialog(db: AppDatabase, adminEmail: String, onDismiss: () -> Unit) {
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
                    modifier = Modifier.fillMaxWidth()
                )

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
                    val priceDouble = price.toDoubleOrNull() ?: 0.0
                    if (name.isBlank() || price.isBlank() || selectedCategory.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show()
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            val p = Product(
                                name = name,
                                price = priceDouble,
                                category = selectedCategory,
                                imageUrl = imgUrl,
                                ownerEmail = adminEmail // QUAN TRỌNG: Lưu email chủ shop
                            )
                            db.productDao().insertProduct(p)
                            withContext(Dispatchers.Main) { onDismiss() }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48C25))
            ) { Text("Lưu") }
        }
    )
}

@Composable
fun CategoryManager(db: AppDatabase) {
    val categoryList by db.categoryDao().getAllCategories().collectAsState(initial = emptyList())
    var newCatName by remember { mutableStateOf("") }
    val context = LocalContext.current

    // State cho việc sửa và xóa
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("Quản lý danh mục", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        Spacer(modifier = Modifier.height(12.dp))

        // Ô nhập thêm danh mục mới
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
                            withContext(Dispatchers.Main) { newCatName = "" }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48C25))
            ) { Text("Thêm") }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Danh sách danh mục:", fontSize = 14.sp, color = Color.Gray)

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(categoryList) { cat ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(cat.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)

                        // Nút Sửa
                        IconButton(onClick = { editingCategory = cat }) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF4CAF50))
                        }

                        // Nút Xóa
                        IconButton(onClick = { categoryToDelete = cat }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                        }
                    }
                }
            }
        }
    }

    // Dialog xác nhận XÓA
    categoryToDelete?.let { cat ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa danh mục '${cat.name}'? Điều này có thể ảnh hưởng đến các sản phẩm thuộc danh mục này.") },
            confirmButton = {
                TextButton(onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        db.categoryDao().deleteCategory(cat)
                        withContext(Dispatchers.Main) { categoryToDelete = null }
                    }
                }) { Text("Xóa", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { categoryToDelete = null }) { Text("Hủy") } }
        )
    }

    // Dialog SỬA tên danh mục
    editingCategory?.let { cat ->
        var updatedName by remember { mutableStateOf(cat.name) }
        AlertDialog(
            onDismissRequest = { editingCategory = null },
            title = { Text("Sửa danh mục") },
            text = {
                OutlinedTextField(
                    value = updatedName,
                    onValueChange = { updatedName = it },
                    label = { Text("Tên mới") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (updatedName.isNotBlank()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            db.categoryDao().updateCategory(cat.copy(name = updatedName.trim()))
                            withContext(Dispatchers.Main) { editingCategory = null }
                        }
                    }
                }) { Text("Cập nhật") }
            },
            dismissButton = { TextButton(onClick = { editingCategory = null }) { Text("Hủy") } }
        )
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
fun AdminProductItem(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Hiển thị ảnh (Dùng Coil)
            coil.compose.AsyncImage(
                model = product.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF1F5F9)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold)
                // Format giá tiền cho đẹp
                Text("${String.format("%,.0f", product.price)}đ", color = Color(0xFFF48C25))
            }

            // NÚT SỬA
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF4CAF50))
            }

            // NÚT XÓA
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(product: Product, db: AppDatabase, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(product.name) }
    var price by remember { mutableStateOf(product.price.toLong().toString()) }
    var imgUrl by remember { mutableStateOf(product.imageUrl) }

    val categoryList by db.categoryDao().getAllCategories().collectAsState(initial = emptyList())
    var selectedCategory by remember { mutableStateOf(product.category) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chỉnh sửa sản phẩm", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên sản phẩm") })
                OutlinedTextField(value = price, onValueChange = { if (it.all { c -> c.isDigit() }) price = it }, label = { Text("Giá tiền") })

                // Dropdown chọn danh mục
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedCategory, onValueChange = {}, readOnly = true,
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
                OutlinedTextField(value = imgUrl, onValueChange = { imgUrl = it }, label = { Text("Link ảnh") })
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank() || price.isBlank()) {
                    Toast.makeText(context, "Không được để trống!", Toast.LENGTH_SHORT).show()
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val updatedProduct = product.copy(
                            name = name,
                            price = price.toDoubleOrNull() ?: 0.0,
                            category = selectedCategory,
                            imageUrl = imgUrl
                        )
                        db.productDao().updateProduct(updatedProduct)
                        withContext(Dispatchers.Main) { onDismiss() }
                    }
                }
            }) { Text("Cập nhật") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}