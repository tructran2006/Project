package com.example.project


import android.R.attr.text
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.project.data.database.AppDatabase
import com.example.project.gui.user.ProfileScreen
import com.example.project.gui.user.SettingsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.project.data.entities.Product
import com.example.project.utils.SharedPrefs
import com.example.project.data.entities.CartItem
import com.example.project.data.entities.Order
import com.example.project.gui.user.MyOrdersScreen
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import java.util.Calendar
import kotlin.collections.sumOf

fun formatPrice(price: Double): String {
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    return formatter.format(price.toInt()) + "đ"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainAppScreen()
        }
    }
}


@Composable
fun MainAppScreen() {
    val context = LocalContext.current
    val primaryColor = Color(0xFFF48C25)
    val bgColor = Color(0xFFF8F7F5)
    val db = remember { AppDatabase.getDatabase(context) }

    var currentTab by remember { mutableStateOf("home") }
    var userRole by remember { mutableStateOf("user") }
    var userName by remember { mutableStateOf("Đang tải...") }
    var userEmail by remember { mutableStateOf("...") }
    var userBirthday by remember { mutableStateOf("") }
    var userAge by remember { mutableStateOf("") }
    var userAddress by remember { mutableStateOf("") }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCartItemIds by remember { mutableStateOf(setOf<Int>()) }

    var selectedCategoryFromHome by remember { mutableStateOf("Tất cả") }
    var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }

    val emailFromIntent = (context as? Activity)?.intent?.getStringExtra("USER_EMAIL") ?: ""
    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(emailFromIntent, refreshTrigger) {
        if (emailFromIntent.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                val user = db.userDao().getUserByEmail(emailFromIntent)
                user?.let {
                    userName = it.username
                    userEmail = it.email
                    userRole = it.role
                    userBirthday = it.birthday ?: "Chưa cập nhật"
                    userAge = it.age?.toString() ?: "0"
                    userAddress = it.address ?: "Chưa cập nhật"
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(primaryColor, currentTab) { currentTab = it }
        },
        containerColor = bgColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            AnimatedContent(targetState = currentTab, label = "") { tab ->
                when (tab) {
                    "home" -> HomeTab(
                        db = db,
                        primary = primaryColor,
                        searchQuery = searchQuery, // Truyền biến vào
                        onSearchChange = { searchQuery = it }, // Truyền hàm thay đổi vào
                        onCategoryClick = {
                            selectedCategoryFromHome = it
                            currentTab = "category"
                        },
                        onProductClick = { selectedProductForDetail = it },
                        onCartClick = { currentTab = "cart" }
                    )

                    "category" -> CategoryScreen(primaryColor, selectedCategoryFromHome)
                    "favorite" -> FavoriteScreen(primaryColor)
                    "cart" -> CartScreen(
                        primary = primaryColor,
                        selectedIds = selectedCartItemIds, // Biến state tại MainAppScreen
                        onIdsChange = { selectedCartItemIds = it }, // Cập nhật state khi tick
                        onCheckout = { currentTab = "checkout" }
                    )

                    "checkout" -> {
                        // 1. Lấy UserId thật từ Email thay vì dùng số 1 cứng nhắc
                        val currentUserId = remember(userEmail) { mutableIntStateOf(1) }
                        LaunchedEffect(userEmail) {
                            withContext(Dispatchers.IO) {
                                db.userDao().getUserByEmail(userEmail)?.id?.let {
                                    currentUserId.intValue = it
                                }
                            }
                        }


                        CheckoutScreen(
                            primary = primaryColor,
                            userId = currentUserId.intValue,
                            selectedIds = selectedCartItemIds.toList(),
                            onNavigateToSettings = { currentTab = "settings" }
                        )
                    }

                    "profile" -> ProfileScreen(
                        db, userName, userEmail, userRole,
                        onNavigateToSettings = { currentTab = "settings" },
                        onNavigateToFavorite = { currentTab = "favorite" },
                        onNavigateToOrders = { currentTab = "my_orders" }
                    )

                    "settings" -> SettingsScreen(
                        userName,
                        userEmail,
                        userRole,
                        userBirthday,
                        userAge,
                        userAddress,
                        { currentTab = "profile" },
                        { refreshTrigger++ })


                    "my_orders" -> MyOrdersScreen(db, userEmail) { currentTab = "profile" }
                }
            }

            selectedProductForDetail?.let {
                ProductDetailDialog(
                    it,
                    { selectedProductForDetail = null },
                    primaryColor
                )
            }
        }
    }
}

//Hiển thị lời chào thay đổi theo thời gian
@Composable
fun HeaderSection(primary: Color, onCartClick: () -> Unit) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Chào buổi sáng ☀️"
        in 12..17 -> "Chào buổi chiều 🌤️"
        else -> "Chào buổi tối 🌙"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(greeting, color = Color(0xFF9C7349), fontSize = 12.sp)
            Text("Khám phá ngay", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(onClick = onCartClick) {
                BadgedBox(badge = {
                    Badge(containerColor = Color.Red) {
                        Text(
                            "!",
                            color = Color.White
                        )
                    }
                }) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeTab(
    db: AppDatabase,
    primary: Color,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    onCartClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .fillMaxSize()
    ) {
        HeaderSection(primary, onCartClick)
        SearchBarSection(
            searchQuery = searchQuery,
            onSearchChange = onSearchChange
        )
        PromoCarousel()
        CategorySection(primary, db, onCategoryClick, onSeeAll = { onCategoryClick("Tất cả") })
        AllProductsSection(
            primary = primary,
            db = db,
            searchQuery = searchQuery,
            onProductClick = onProductClick
        )
    }
}

//Thanh tìm kiếm sản phẩm
@Composable
fun CategorySection(
    primary: Color,
    db: AppDatabase,
    onCategoryClick: (String) -> Unit,
    onSeeAll: () -> Unit
) {
    val allProducts by db.productDao().getAllProducts().collectAsState(initial = emptyList())
    val categories = remember(allProducts) { allProducts.map { it.category }.distinct() }

    Column(modifier = Modifier.padding(top = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Danh mục sản phẩm", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            TextButton(onClick = onSeeAll) {
                Text(
                    "Xem tất cả",
                    color = primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        LazyRow(
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(categories) { catName ->
                Box(modifier = Modifier.clickable { onCategoryClick(catName) }) {
                    CategoryItem(catName, Icons.Default.Category, Color(0xFFFFEDD5), primary)
                }
            }
        }
    }
}

@Composable
fun SearchBarSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        placeholder = { Text("Tìm kiếm sản phẩm...", color = Color(0xFF9C7349)) },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = Color(0xFF9C7349)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color(0xFFF48C25)
        )
    )
}

//Hiển thị các banner quảng cáo
@Composable
fun PromoCarousel() {
    LazyRow(
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PromoCard("Siêu Sale Hè!", "Giảm đến 50%", Color(0xFFF48C25))
        }
        item {
            PromoCard("Hàng Mới Về", "BST Thu Đông 2026", Color(0xFF3B82F6))
        }
    }
}

@Composable
fun PromoCard(title: String, sub: String, bg: Color) {
    Box(
        modifier = Modifier
            .width(300.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(sub, color = Color.White.copy(0.8f), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text("Mua ngay", color = bg, fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
fun CategoryItem(name: String, icon: ImageVector, bg: Color, tint: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(32.dp))
        }
        Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

//Danh mục
@Composable
fun CategoryScreen(primary: Color, initialCategory: String) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val allProducts by db.productDao().getAllProducts().collectAsState(initial = emptyList())
    val categoryList =
        remember(allProducts) { listOf("Tất cả") + allProducts.map { it.category }.distinct() }
    var selectedCategory by remember(initialCategory) { mutableStateOf(initialCategory) }

    val filteredProducts =
        if (selectedCategory == "Tất cả") allProducts else allProducts.filter { it.category == selectedCategory }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Danh mục sản phẩm", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(categoryList) { item ->
                Button(
                    onClick = { selectedCategory = item },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedCategory == item) primary else Color.LightGray),
                    shape = RoundedCornerShape(12.dp)
                ) { Text(item, color = Color.White) }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Dùng LazyColumn thay vì Column + verticalScroll để tránh lỗi Crash luồng UI
        androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredProducts) { product ->
                CategoryProductCard(product, primary)
            }
        }
    }
}

@Composable
fun CategoryProductCard(product: Product, primary: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            coil.compose.AsyncImage(
                model = product.imageUrl.ifBlank { "https://via.placeholder.com/150" },
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = product.category, color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formatPrice(product.price),
                    color = primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


//Mục Yêu thích
@Composable
fun FavoriteScreen(primary: Color) {

    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }

    // Lấy toàn bộ sản phẩm từ DB
    val allProducts by db.productDao()
        .getAllProducts()
        .collectAsState(initial = emptyList())

    var refresh by remember { mutableIntStateOf(0) }

    // Lấy danh sách ID yêu thích từ SharedPrefs
    val favoriteIds = remember(refresh) {
        SharedPrefs.getFavoriteIds(context)
    }

    // Lọc những sản phẩm nào có ID nằm trong danh sách yêu thích
    val favoriteProducts = allProducts.filter {
        favoriteIds.contains(it.id.toString())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "Sản phẩm yêu thích",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (favoriteProducts.isEmpty()) {
            Text("Chưa có sản phẩm yêu thích", color = Color.Gray)
        } else {

            favoriteProducts.forEach { product ->

                FavoriteProductCard(
                    product = product,
                    primary = primary,
                    onRemove = {
                        SharedPrefs.removeFavorite(context, product.id)
                        refresh++ // reload
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}


// Card hiển thị sản phẩm trong mục yêu thích
// Dùng lại dữ liệu thật từ Product để đảm bảo ảnh, tên, giá đồng bộ với trang chủ
@Composable
fun FavoriteProductCard(
    product: Product,
    primary: Color,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hiển thị ảnh sản phẩm từ đường link imageUrl
            // Nếu link rỗng thì dùng ảnh placeholder mặc định
            AsyncImage(
                model = if (product.imageUrl.isNotBlank()) product.imageUrl else "https://via.placeholder.com/150",
                contentDescription = product.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE0F2FE)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Tên sản phẩm
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Danh mục sản phẩm
                Text(
                    text = product.category,
                    color = Color.Gray,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Giá sản phẩm
                Text(
                    text = formatPrice(product.price),
                    color = primary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Nút bỏ khỏi yêu thích
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Bỏ yêu thích",
                    tint = Color.Red
                )
            }
        }
    }
}

//Mục Giỏ hàng
@Composable
fun CartScreen(
    primary: Color,
    selectedIds: Set<Int>,
    onIdsChange: (Set<Int>) -> Unit,
    onCheckout: () -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    val activity = context as? Activity
    val userEmail = activity?.intent?.getStringExtra("USER_EMAIL") ?: ""
    var userId by remember { mutableIntStateOf(1) }

    val allProducts by db.productDao().getAllProducts().collectAsState(initial = emptyList())
    val productMap = remember(allProducts) { allProducts.associateBy { it.id } }


    LaunchedEffect(userEmail) {
        if (userEmail.isNotEmpty()) {
            val user = withContext(Dispatchers.IO) { db.userDao().getUserByEmail(userEmail) }
            userId = user?.id ?: 1
        }
    }

    val cartItems by db.productDao().getCartFlow(userId).collectAsState(initial = emptyList())
    var selectedItemIds by remember { mutableStateOf(setOf<Int>()) }


    val totalAmount by remember(cartItems, selectedIds, allProducts) {
        derivedStateOf {
            val map = allProducts.associateBy { it.id }
            cartItems
                .filter { selectedIds.contains(it.productId) }
                .sumOf { item ->
                    (map[item.productId]?.price ?: 0.0) * item.quantity
                }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. PHẦN DANH SÁCH (Chiếm hết chỗ trống ở giữa)
        Box(modifier = Modifier
            .weight(1f)
            .padding(horizontal = 16.dp, vertical = 8.dp)) {
            if (cartItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Giỏ hàng đang trống", color = Color.Gray)
                }
            } else {
                Column {
                    Text("Giỏ hàng của bạn", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                    LazyColumn {
                        items(cartItems) { item ->
                            val product = productMap[item.productId]
                            if (product != null) {
                                CartProductItem(
                                    product = product,
                                    quantity = item.quantity,
                                    primary = primary,
                                    isSelected = selectedIds.contains(item.productId),
                                    onCheckedChange = { isChecked ->
                                        val newIds = if (isChecked) selectedIds + item.productId else selectedIds - item.productId
                                        onIdsChange(newIds)
                                    },
                                    onQuantityChange = { newQty ->
                                        scope.launch(Dispatchers.IO) { db.productDao().updateCartQuantity(item.cartId, newQty) }
                                    },
                                    onDelete = {
                                        scope.launch(Dispatchers.IO) { db.productDao().deleteCartItem(item) }
                                    }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tổng thanh toán:", fontWeight = FontWeight.Medium, color = Color.Gray)
                    Text(
                        formatPrice(totalAmount),
                        color = primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onCheckout() },
                    enabled = selectedIds.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    Text("Thanh toán (${selectedIds.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}


@Composable
fun CartProductItem(
    product: Product,
    quantity: Int,
    primary: Color,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onQuantityChange: (Int) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // CHECKBOX CHỌN SẢN PHẨM
            Checkbox(
                checked = isSelected,
                onCheckedChange = { checked ->
                    onCheckedChange(checked)
                },
                colors = CheckboxDefaults.colors(checkedColor = primary)
            )


            AsyncImage(
                model = product.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(text = formatPrice(product.price), color = primary)

                // BỘ TĂNG GIẢM SỐ LƯỢNG
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.RemoveCircleOutline,
                            contentDescription = null,
                            tint = primary
                        )
                    }

                    Text(
                        text = "$quantity",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { onQuantityChange(quantity + 1) },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.AddCircleOutline,
                            contentDescription = null,
                            tint = primary
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.LightGray)
            }
        }
    }
}


//Thanh toán
@Composable
fun CheckoutScreen(
    primary: Color,
    userId: Int,
    selectedIds: List<Int>,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

    // --- BƯỚC 1: LẤY DỮ LIỆU TƯƠI TỪ DATABASE (Sử dụng Flow) ---
    // Không dùng LaunchedEffect nữa, dùng collectAsState để dữ liệu tự nhảy
    val cartFlow by db.productDao().getCartFlow(userId).collectAsState(initial = emptyList())
    val allProducts by db.productDao().getAllProducts().collectAsState(initial = emptyList())
    val productMap = remember(allProducts) { allProducts.associateBy { it.id } }

    // Lọc ra những sản phẩm người dùng đã chọn bên Giỏ hàng
    val cartItems = remember(cartFlow, selectedIds) {
        cartFlow.filter { it.productId in selectedIds }
    }

    // --- BƯỚC 2: TÍNH TIỀN TỰ ĐỘNG ---
    val totalPrice by remember(cartItems, productMap) {
        derivedStateOf {
            cartItems.sumOf { (productMap[it.productId]?.price ?: 0.0) * it.quantity }
        }
    }

    // --- BƯỚC 3: THÔNG TIN NGƯỜI DÙNG ---
    var userAddress by remember { mutableStateOf("") }
    var userPhone by remember { mutableStateOf("") }
    var isOrdering by remember { mutableStateOf(false) }
    var paymentMethod by remember { mutableStateOf("COD") }

    val activity = context as? Activity
    val email = activity?.intent?.getStringExtra("USER_EMAIL") ?: ""

    // Chỉ dùng LaunchedEffect để load thông tin SĐT và Địa chỉ
    LaunchedEffect(email) {
        withContext(Dispatchers.IO) {
            val user = db.userDao().getUserByEmail(email)
            user?.let {
                userAddress = it.address ?: ""
                userPhone = it.phone ?: ""
            }
        }
    }

    val shippingFee = 30000.0
    val voucher = 10000.0
    val finalPrice = totalPrice + shippingFee - voucher

    val displayPhone = userPhone.ifEmpty { "Chưa có SĐT" }
    val displayAddress = userAddress.ifEmpty { "Chưa có địa chỉ" }
    Scaffold(
        containerColor = Color(0xFFF6F7FB),
        bottomBar = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tổng thanh toán",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                        Text(
                            text = (formatPrice(finalPrice)),
                            color = Color(0xFFE53935),
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }

                    Button(
                        onClick = {
                            if (isOrdering) return@Button

                            scope.launch(Dispatchers.IO) {
                                isOrdering = true
                                try {
                                    // 1. TẠO CHUỖI MÔ TẢ SẢN PHẨM (Để sửa lỗi productDescription)
                                    val description = cartItems.joinToString(separator = ", ") { item ->
                                        val product = productMap[item.productId]
                                        "${item.quantity}x ${product?.name ?: "Sản phẩm ${item.productId}"}"
                                    }

                                    // 2. LẤY EMAIL CỦA SHOP (Để sửa lỗi shopEmail)
                                    // Lấy từ sản phẩm đầu tiên trong giỏ hàng
                                    val firstProduct = productMap[cartItems.firstOrNull()?.productId ?: -1]
                                    val emailOfShop = firstProduct?.ownerEmail ?: "admin@gmail.com"

                                    // 3. GỌI HÀM VỚI ĐẦY ĐỦ THAM SỐ
                                    db.productDao().createOrder(
                                        Order(
                                            userId = userId,
                                            shopEmail = emailOfShop,        // Đã thêm
                                            productDescription = description, // Đã thêm
                                            totalPrice = finalPrice,
                                            status = "Pending",
                                            note = "Đơn hàng từ App"
                                        )
                                    )

                                    db.productDao().clearCart(userId)

                                    withContext(Dispatchers.Main) {
                                        // Cập nhật UI sau khi đặt hàng thành công
                                        Toast.makeText(context, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show()
                                        // Có thể điều hướng về trang chủ ở đây
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Lỗi đặt hàng: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                } finally {
                                    isOrdering = false
                                }
                            }
                        },
                        enabled = cartItems.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = primary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text(
                            text = if (isOrdering) "Đang xử lý..." else "Đặt hàng",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = "Thanh toán",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Địa chỉ nhận hàng
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = primary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Địa chỉ nhận hàng",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = displayPhone,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = displayAddress,
                            color = Color.Gray,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sản phẩm
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Sản phẩm",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (cartItems.isEmpty()) {
                        Text(
                            text = "Chưa có sản phẩm trong giỏ hàng",
                            color = Color.Gray
                        )
                    } else {
                        cartItems.forEachIndexed { index, item ->
                            val product = productMap[item.productId]
                            val itemPrice = (product?.price ?: 0.0)
                            val itemTotal = itemPrice * item.quantity

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                coil.compose.AsyncImage(
                                    model = product?.imageUrl?.ifBlank { "https://via.placeholder.com/150" }
                                        ?: "https://via.placeholder.com/150",
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF0F1F5))
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product?.name ?: "Sản phẩm ${item.productId}",
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = formatPrice(itemPrice),
                                        color = Color(0xFFE53935),
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "Số lượng: ${item.quantity}",
                                        color = Color.Gray,
                                        fontSize = 13.sp
                                    )
                                }

                                Text(
                                    text = formatPrice(itemTotal),
                                    fontWeight = FontWeight.Bold,
                                    color = primary
                                )
                            }

                            if (index != cartItems.lastIndex) {
                                HorizontalDivider(color = Color(0xFFEDEEF3))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Voucher
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Voucher của shop",
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "-${formatPrice(voucher)}",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Phí ship
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = primary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Phí vận chuyển",
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = formatPrice(shippingFee),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Phương thức thanh toán
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Phương thức thanh toán",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = paymentMethod == "COD",
                            onClick = { paymentMethod = "COD" }
                        )
                        Column {
                            Text("Thanh toán khi nhận hàng")
                            Text(
                                "Thanh toán tiền mặt lúc nhận hàng",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = paymentMethod == "BANK",
                            onClick = { paymentMethod = "BANK" }
                        )
                        Column {
                            Text("Chuyển khoản")
                            Text(
                                "Thanh toán qua tài khoản ngân hàng",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (paymentMethod == "COD")
                            "Đang chọn: Thanh toán khi nhận hàng"
                        else
                            "Đang chọn: Chuyển khoản",
                        color = primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tóm tắt tiền
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Chi tiết thanh toán",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tạm tính", color = Color.Gray)
                        Text(formatPrice(totalPrice))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Phí vận chuyển", color = Color.Gray)
                        Text(formatPrice(shippingFee))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Voucher", color = Color.Gray)
                        Text("-${formatPrice(voucher)}")
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFFEDEEF3))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Thành tiền",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = (formatPrice(finalPrice)),
                            color = Color(0xFFE53935),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}



//Hiển thị sản phẩm
@Composable
fun AllProductsSection(
    primary: Color,
    db: AppDatabase,
    searchQuery: String,
    onProductClick: (Product) -> Unit
) {
    val allProducts by db.productDao().getAllProducts().collectAsState(initial = emptyList())

    // Logic lọc: Nếu gõ chữ thì lọc, nếu trống thì hiện hết
    val filteredProducts = remember(allProducts, searchQuery) {
        if (searchQuery.isEmpty()) {
            allProducts
        } else {
            allProducts.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = if (searchQuery.isEmpty()) "Tất cả sản phẩm" else "Kết quả cho '$searchQuery'",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (filteredProducts.isEmpty()) {
            Text(
                "Không tìm thấy sản phẩm nào",
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 20.dp)
            )
        } else {
            filteredProducts.chunked(2).forEach { rowProducts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowProducts.forEach { product ->
                        ProductCard(
                            product = product,
                            primary = primary,
                            modifier = Modifier.weight(1f),
                            onClick = { onProductClick(product) }
                        )
                    }
                    if (rowProducts.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

//Thẻ hiển thị sản phẩm nhỏ gọn với nút "Thêm vào giỏ" nhanh và nút "Yêu thích"
@Composable
fun ProductCard(product: Product, primary: Color, modifier: Modifier, onClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isFavorite by remember { mutableStateOf(SharedPrefs.isFavorite(context, product.id)) }

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
            Text(formatPrice(product.price), color = primary, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        SharedPrefs.toggleFavorite(context, product.id); isFavorite = !isFavorite
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFFEE2E2), CircleShape)
                ) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        null,
                        tint = Color.Red,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = {
                    val db = AppDatabase.getDatabase(context)
                    scope.launch(Dispatchers.IO) {
                        val activity = context as? Activity
                        val userEmail = activity?.intent?.getStringExtra("USER_EMAIL") ?: ""
                        val user = db.userDao().getUserByEmail(userEmail)
                        val uId = user?.id ?: 1
                        val existing = db.productDao().getCartItem(uId, product.id)
                        if (existing != null) db.productDao()
                            .updateCartQuantity(existing.cartId, existing.quantity + 1)
                        else db.productDao()
                            .addToCart(CartItem(userId = uId, productId = product.id, quantity = 1))
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Đã thêm vào giỏ!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }, modifier = Modifier
                    .size(32.dp)
                    .background(primary, CircleShape)) {
                    Icon(
                        Icons.Default.AddShoppingCart,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

//chi tiết sản phẩm
@Composable
fun ProductDetailDialog(product: Product, onDismiss: () -> Unit, primary: Color) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onDismiss) { Text("Đóng") } },
        title = { Text(product.name, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                coil.compose.AsyncImage(
                    model = product.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Giá: ${formatPrice(product.price)}",
                    color = primary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Text("Danh mục: ${product.category}", color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Shop bán: ${product.ownerEmail}",
                    fontWeight = FontWeight.Medium,
                    color = Color.Blue
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(product.description, fontSize = 14.sp)
            }
        }
    )
}

//Thanh điều hướng dưới cùng giúp chuyển đổi nhanh giữa 5 mục chính
@Composable
fun BottomNavigationBar(primaryColor: Color, currentTab: String, onTabClick: (String) -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        val items = listOf(
            Triple("home", "Trang chủ", Icons.Default.Home),
            Triple("category", "Danh mục", Icons.Default.GridView),
            Triple("favorite", "Yêu thích", Icons.Default.Favorite),
            Triple(
                "cart",
                "Giỏ hàng",
                Icons.Default.ShoppingCart
            ), // Giỏ hàng đứng trước Cá nhân
            Triple("profile", "Cá nhân", Icons.Default.Person)
        )

        items.forEach { (tab, label, icon) ->
            NavigationBarItem(
                selected = currentTab == tab,
                onClick = { onTabClick(tab) },
                icon = { Icon(icon, null) },
                label = { Text(label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = primaryColor,
                    selectedTextColor = primaryColor
                )
            )
        }
    }
}
