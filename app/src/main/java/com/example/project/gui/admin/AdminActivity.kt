package com.example.project.gui.admin

import android.R.attr.description
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
import com.example.project.data.entities.Order
import com.example.project.data.entities.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextOverflow
import com.example.project.formatPrice

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
    val activity = context.findActivity()

    // Lấy thông tin từ Intent (Gửi từ Login hoặc Settings)
    val userEmail = activity?.intent?.getStringExtra("USER_EMAIL") ?: ""
    val userRole = activity?.intent?.getStringExtra("USER_ROLE") ?: "shop"

    // Định nghĩa Tab dựa trên Role
    val tabs = if (userRole == "admin") listOf("Danh mục", "Đơn hệ thống")
    else listOf("Sản phẩm của tôi", "Đơn khách đặt")

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            if (userRole == "admin") "Hệ Thống Quản Trị" else "Kênh Người Bán",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { activity?.finish() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White
                    )
                )
                TabRow(selectedTabIndex = selectedTab, containerColor = Color.White, contentColor = Color(0xFFF48C25)) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(Color(0xFFF8F7F5))) {
            if (userRole == "admin") {
                when (selectedTab) {
                    0 -> CategoryManager(db)
                    1 -> OrderManager(db, userEmail, isAdmin = true)
                }
            } else {
                when (selectedTab) {
                    0 -> ProductManager(db, userEmail)
                    1 -> OrderManager(db, userEmail, isAdmin = false)
                }
            }
        }
    }
}

@Composable
fun ProductManager(db: AppDatabase, shopEmail: String) {
    val productList by db.productDao().getProductsByOwnerFlow(shopEmail).collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (productList.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                Text("Bạn chưa đăng sản phẩm nào", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                items(productList) { product ->
                    AdminProductItem(
                        product = product,
                        onEdit = { editingProduct = product },
                        onDelete = {
                            CoroutineScope(Dispatchers.IO).launch { db.productDao().deleteProduct(product) }
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = Color(0xFFF48C25)
        ) { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) }
    }

    if (showAddDialog) {
        AddProductWithImageDialog(db = db, adminEmail = shopEmail, onDismiss = { showAddDialog = false })
    }

    editingProduct?.let { product ->
        EditProductDialog(db = db, product = product, onDismiss = { editingProduct = null })
    }
}

fun mapStatusToVietnamese(status: String): Pair<String, Color> {
    return when (status) {
        "Pending" -> "Chờ xác nhận" to Color(0xFFF48C25) // Cam
        "Processing" -> "Đang chuẩn bị" to Color(0xFF3B82F6) // Xanh dương
        "Shipping" -> "Đang giao hàng" to Color(0xFF2196F3) // Xanh da trời
        "Completed" -> "Hoàn tất" to Color(0xFF4CAF50) // Xanh lá
        "Cancelled" -> "Đã hủy" to Color(0xFFE53935) // Đỏ
        else -> status to Color.Gray
    }
}
@Composable
fun OrderManager(db: AppDatabase, email: String, isAdmin: Boolean) {
    val orderList by (if (isAdmin) db.productDao().getAllOrdersFlow()
    else db.productDao().getOrdersForShopFlow(email))
        .collectAsState(initial = emptyList())

    if (orderList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Chưa có đơn hàng nào", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            items(orderList) { order ->
                val (statusText, statusColor) = mapStatusToVietnamese(order.status)

                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Mã đơn: #${order.id}", fontWeight = FontWeight.Bold)
                            // HIỂN THỊ TIẾNG VIỆT
                            Text(statusText, color = statusColor, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("🛒 ${order.productDescription}", fontSize = 14.sp, color = Color.DarkGray)
                        Text("Tổng tiền: ${formatPrice(order.totalPrice)}", fontWeight = FontWeight.Bold, color = Color(0xFFF48C25))

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val scope = rememberCoroutineScope()

                            // 1. Nút Hủy đơn (Có nền đỏ - Hiện khi đơn chưa hoàn thành và chưa bị hủy)
                            if (order.status != "Completed" && order.status != "Cancelled") {
                                Button(
                                    onClick = {
                                        scope.launch(Dispatchers.IO) { db.productDao().updateOrderStatus(order.id, "Cancelled") }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) {
                                    Text("Hủy đơn", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // 2. Nút Duyệt đơn (Khi đang chờ - Pending)
                            if (order.status == "Pending") {
                                Button(
                                    onClick = {
                                        scope.launch(Dispatchers.IO) { db.productDao().updateOrderStatus(order.id, "Processing") }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Duyệt đơn", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // 3. Nút Giao hàng (Khi đã chuẩn bị xong - Processing)
                            if (order.status == "Processing") {
                                Button(
                                    onClick = {
                                        scope.launch(Dispatchers.IO) { db.productDao().updateOrderStatus(order.id, "Shipping") }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Giao hàng", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // 4. Nút Hoàn tất (Khi đang giao hàng - Shipping)
                            if (order.status == "Shipping") {
                                Button(
                                    onClick = {
                                        scope.launch(Dispatchers.IO) { db.productDao().updateOrderStatus(order.id, "Completed") }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Hoàn tất", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductWithImageDialog(db: AppDatabase, adminEmail: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imgUrl by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    val categoryList by db.categoryDao().getAllCategories().collectAsState(initial = emptyList())

    LaunchedEffect(categoryList) {
        if (categoryList.isNotEmpty() && selectedCategory.isEmpty()) {
            selectedCategory = categoryList[0].name
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm sản phẩm", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên sản phẩm") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { if (it.all { c -> c.isDigit() }) price = it }, label = { Text("Giá tiền") }, modifier = Modifier.fillMaxWidth())

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
                OutlinedTextField(value = imgUrl, onValueChange = { imgUrl = it }, label = { Text("Link ảnh") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả sản phẩm") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val pDouble = price.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank() && price.isNotBlank()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        db.productDao().insertProduct(Product(name = name, price = pDouble, category = selectedCategory, imageUrl = imgUrl, ownerEmail = adminEmail,description = description))
                        withContext(Dispatchers.Main) { onDismiss() }
                    }
                }
            }) { Text("Lưu") }
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


fun updateStatus(db: AppDatabase, orderId: Int, newStatus: String) {
    CoroutineScope(Dispatchers.IO).launch {
        db.orderDao().updateOrderStatus(orderId, newStatus)
    }
}
@Composable
fun AdminProductItem(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
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