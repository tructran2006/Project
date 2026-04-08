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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    val tabs = if (userRole == "admin") {
        listOf("Thống kê", "Danh mục", "Đơn hệ thống")
    } else {
        listOf("Thống kê", "Sản phẩm", "Đơn khách")
    }

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
                    // quay về trang chủ
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
                    0 -> AdminDashboardTab(db, userRole)
                    1 -> CategoryManager(db)
                    2 -> OrderManager(db, userEmail, isAdmin = true)
                }
            } else {
                when (selectedTab) {
                    0 -> AdminDashboardTab(db, userRole)
                    1 -> ProductManager(db, userEmail)
                    2 -> OrderManager(db, userEmail, isAdmin = false)
                }
            }
        }
    }
}

// quản lí sản phẩm
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
        // nút tạo sản phẩm
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = Color(0xFFF48C25)
        ) { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) }
    }
    // gọi hàm tạo sản phẩm
    if (showAddDialog) {
        AddProductWithImageDialog(db = db, adminEmail = shopEmail, onDismiss = { showAddDialog = false })
    }
    // gọi hàm chỉnh sửa sản phẩm
    editingProduct?.let { product ->
        EditProductDialog(db = db, product = product, onDismiss = { editingProduct = null })
    }
}

// trạng thái của đơn hàng
fun mapStatus(status: String): Pair<String, Color> {
    return when (status) {
        "Pending" -> "Chờ xác nhận" to Color(0xFFF48C25) // Cam
        "Processing" -> "Đang chuẩn bị" to Color(0xFF3B82F6) // Xanh dương
        "Shipping" -> "Đang giao hàng" to Color(0xFF2196F3) // Xanh da trời
        "Completed" -> "Hoàn tất" to Color(0xFF4CAF50) // Xanh lá
        "Cancelled" -> "Đã hủy" to Color(0xFFE53935) // Đỏ
        else -> status to Color.Gray
    }
}

//quản lí đơn hàng
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
                val (statusText, statusColor) = mapStatus(order.status)

                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Mã đơn: #${order.id}", fontWeight = FontWeight.Bold)
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

                            // 1. Nút Hủy đơn
                            if (order.status != "Completed" && order.status != "Cancelled") {
                                Button(
                                    // gọi hàm thay đổi trạng thái trong ProductDao
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

                            // 2. Nút Duyệt đơn
                            if (order.status == "Pending") {
                                Button(
                                    // gọi hàm thay đổi trạng thái trong ProductDao
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

                            // 3. Nút Giao hàng
                            if (order.status == "Processing") {
                                Button(
                                    // gọi hàm thay đổi trạng thái trong ProductDao
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

                            // 4. Nút Hoàn tất
                            if (order.status == "Shipping") {
                                Button(
                                    // gọi hàm thay đổi trạng thái trong ProductDao
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

//hàm tạo sản phẩm với hình và mô tả
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductWithImageDialog(db: AppDatabase, adminEmail: String, onDismiss: () -> Unit) {

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
        // kiểm tra điều kiện của thêm sản phẩm
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

//hàm tạo danh mục
@Composable
fun CategoryManager(db: AppDatabase) {
    val categoryList by db.categoryDao().getAllCategories().collectAsState(initial = emptyList())
    var newCatName by remember { mutableStateOf("") }


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


//hàm chỉnh sửa đơn hàng
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(product: Product, db: AppDatabase, onDismiss: () -> Unit) {
    val context = LocalContext.current


    var name by remember { mutableStateOf(product.name) }
    var price by remember { mutableStateOf(product.price.toLong().toString()) }
    var imgUrl by remember { mutableStateOf(product.imageUrl) }
    var description by remember { mutableStateOf(product.description) } // Thêm dòng này

    val categoryList by db.categoryDao().getAllCategories().collectAsState(initial = emptyList())
    var selectedCategory by remember { mutableStateOf(product.category) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chỉnh sửa sản phẩm", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên sản phẩm") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { if (it.all { c -> c.isDigit() }) price = it }, label = { Text("Giá tiền") }, modifier = Modifier.fillMaxWidth())

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

                OutlinedTextField(value = imgUrl, onValueChange = { imgUrl = it }, label = { Text("Link ảnh") }, modifier = Modifier.fillMaxWidth())


                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả sản phẩm") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank() || price.isBlank() || description.isBlank()) {
                    Toast.makeText(context, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val updatedProduct = product.copy(
                            name = name,
                            price = price.toDoubleOrNull() ?: 0.0,
                            category = selectedCategory,
                            imageUrl = imgUrl,
                            description = description
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

//trang thống kê
@Composable
fun AdminDashboardTab(db: AppDatabase, userRole: String) {
    // --- Dữ liệu cho Shop ---
    val totalRevenue by db.productDao().getTotalRevenue().collectAsState(initial = 0.0)
    val pendingCount by db.productDao().getPendingOrderCount().collectAsState(initial = 0)
    val shopProductCount by db.productDao().getTotalProductCount().collectAsState(initial = 0)

    // --- Dữ liệu cho Admin ---
    val totalUsers by db.userDao().getTotalUserCount().collectAsState(initial = 0)
    val totalShops by db.userDao().getTotalShopCount().collectAsState(initial = 0)
    val systemProductCount by db.productDao().getSystemProductCount().collectAsState(initial = 0)
    val totalCategories by db.categoryDao().getAllCategories().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (userRole == "admin") "Thống kê hệ thống" else "Thống kê kinh doanh",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = Color(0xFFF48C25)
        )

        if (userRole == "admin") {
            //trang thống kê của admin
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // lấy từ hàm getTotalUserCount() trong UserDao
                    DashboardSmallCard("Người dùng", "$totalUsers", Icons.Default.People, Color(0xFF4CAF50), Modifier.weight(1f))
                    // lấy từ hàm getTotalShopCount() trong UserDao
                    DashboardSmallCard("Cửa hàng", "$totalShops", Icons.Default.Store, Color(0xFF2196F3), Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    //lấy từ hàm getSystemProductCount() trong ProductDao
                    DashboardSmallCard("Sản phẩm", "$systemProductCount", Icons.Default.Inventory2, Color(0xFF9C27B0), Modifier.weight(1f))
                    // lấy từ hàm getAllCategories() trong CategoryDao
                    DashboardSmallCard("Danh mục", "${totalCategories.size}", Icons.Default.Category, Color(0xFFE91E63), Modifier.weight(1f))
                }
            }
        } else {
            // trang thống kê của shop
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.MonetizationOn, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(50.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tổng doanh thu", fontSize = 15.sp, color = Color.Gray)
                    //lấy hàm getTotalRevenue() trong ProductDao
                    Text(formatPrice(totalRevenue ?: 0.0), fontSize = 30.sp, fontWeight = FontWeight.Black, color = Color(0xFF4CAF50))
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // lấy từ hàm getPendingOrderCount() trong ProductDao
                DashboardSmallCard("Đơn chờ", "$pendingCount", Icons.Default.PendingActions, Color(0xFFF48C25), Modifier.weight(1f))
                // lấy từ hàm getTotalProductCount() trong ProductDao
                DashboardSmallCard("Sản phẩm", "$shopProductCount", Icons.Default.Inventory2, Color(0xFF2196F3), Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DashboardSmallCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier.height(140.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}