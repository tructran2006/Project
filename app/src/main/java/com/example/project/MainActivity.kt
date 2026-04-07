package com.example.project


import android.app.Activity
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import java.util.Calendar



fun formatPrice(price: Double): String {
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    return formatter.format(price.toInt()) + "đ"
}

fun isValidVietnamesePhoneNumber(phone: String): Boolean {
    val regex = Regex("^(0|\\+84)(3|5|7|8|9)([0-9]{8})\$")
    return regex.matches(phone.replace(" ", ""))
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MainAppScreen() }
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
    var userPhone by remember { mutableStateOf("") } 

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
                    userAddress = it.address ?: ""
                    userPhone = it.phone ?: ""
                }
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(primaryColor, currentTab) { currentTab = it } },
        containerColor = bgColor
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            AnimatedContent(targetState = currentTab, label = "") { tab ->
                when (tab) {
                    "home" -> HomeTab(db, primaryColor, searchQuery, { searchQuery = it }, { selectedCategoryFromHome = it; currentTab = "category" }, { selectedProductForDetail = it }, { currentTab = "cart" })

                    "category" -> CategoryScreen(
                        primaryColor,
                        selectedCategoryFromHome,
                        onProductClick = { selectedProductForDetail = it }
                    )
                    "favorite" -> FavoriteScreen(primaryColor)
                    "cart" -> CartScreen(primaryColor, selectedCartItemIds, { selectedCartItemIds = it }, { currentTab = "checkout" })
                    "checkout" -> {
                        val currentUserId = remember(userEmail) { mutableIntStateOf(1) }
                        LaunchedEffect(userEmail) {
                            withContext(Dispatchers.IO) {
                                db.userDao().getUserByEmail(userEmail)?.id?.let { currentUserId.intValue = it }
                            }
                        }
                        CheckoutScreen(
                            primary = primaryColor,
                            userId = currentUserId.intValue,
                            selectedIds = selectedCartItemIds.toList(),
                            onNavigateToSettings = { currentTab = "settings" },
                            onViewOrders = { currentTab = "my_orders" }
                        )
                    }
                    "profile" -> ProfileScreen(db, userName, userEmail, userRole, { currentTab = "settings" }, { currentTab = "favorite" }, { currentTab = "my_orders" })
                    "settings" -> SettingsScreen(userName, userEmail, userRole, userBirthday, userAge, userAddress, userPhone, { currentTab = "profile" }, { refreshTrigger++ })
                    "my_orders" -> MyOrdersScreen(db, userEmail) { currentTab = "profile" }
                }
            }

            selectedProductForDetail?.let { ProductDetailDialog(it, { selectedProductForDetail = null }, primaryColor) }
        }
    }
}

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
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(greeting, color = Color(0xFF9C7349), fontSize = 12.sp)
            Text("Khám phá ngay", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        IconButton(onClick = onCartClick) {
            BadgedBox(badge = { Badge(containerColor = Color.Red) { Text("!", color = Color.White) } }) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = primary, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
fun HomeTab(db: AppDatabase, primary: Color, searchQuery: String, onSearchChange: (String) -> Unit, onCategoryClick: (String) -> Unit, onProductClick: (Product) -> Unit, onCartClick: () -> Unit) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxSize()) {
        HeaderSection(primary, onCartClick)
        SearchBarSection(searchQuery, onSearchChange)
        CategorySection(primary, db, onCategoryClick) { onCategoryClick("Tất cả") }
        AllProductsSection(primary, db, searchQuery, onProductClick)
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

        placeholder = {
            Text("Tìm kiếm sản phẩm...", color = Color(0xFF9C7349))
        },

        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
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


@Composable
fun AllProductsSection(primary: Color, db: AppDatabase, searchQuery: String, onProductClick: (Product) -> Unit) {
    val allProducts by db.productDao().getAllProducts().collectAsState(initial = emptyList())
    //tìm theo tên
    val filteredProducts = remember(allProducts, searchQuery) { if (searchQuery.isEmpty()) allProducts else allProducts.filter { it.name.contains(searchQuery, true) } }
    Column(modifier = Modifier.padding(16.dp)) {
        Text(if (searchQuery.isEmpty()) "Tất cả sản phẩm" else "Kết quả cho '$searchQuery'", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))
        filteredProducts.chunked(2).forEach { row -> Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) { row.forEach { product -> ProductCard(product, primary, Modifier.weight(1f), { onProductClick(product) }) }; if (row.size == 1) Spacer(Modifier.weight(1f)) }; Spacer(modifier = Modifier.height(16.dp)) }
    }
}

//action danh mục ở trang chủ
@Composable
fun CategorySection(primary: Color, db: AppDatabase, onCategoryClick: (String) -> Unit, onSeeAll: () -> Unit) {
    val allProducts by db.productDao().getAllProducts().collectAsState(initial = emptyList())
    val categories = remember(allProducts) { allProducts.map { it.category }.distinct() }

    val randomIcons = listOf(Icons.Default.Stars, Icons.Default.ThumbUp, Icons.Default.Bolt, Icons.Default.AutoAwesome, Icons.Default.Celebration)

    Column(modifier = Modifier.padding(top = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Danh mục sản phẩm", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            TextButton(onClick = onSeeAll) { Text("Xem tất cả", color = primary, fontWeight = FontWeight.Bold) }
        }
        LazyRow(contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            items(categories) { catName ->

                val categoryIcon = remember(catName) {
                    when {
                        catName.contains("công nghệ", true) -> Icons.Default.Smartphone
                        catName.contains("Laptop", true) -> Icons.Default.Laptop
                        catName.contains("thời trang", true) || catName.contains("Quần", true) -> Icons.Default.Checkroom
                        catName.contains("thực phẩm", true) -> Icons.Default.Restaurant
                        else -> randomIcons.random()
                    }
                }

                Box(modifier = Modifier.clickable { onCategoryClick(catName) }) {
                    CategoryItem(catName, categoryIcon, Color(0xFFFFEDD5), primary)
                }
            }
        }
    }
}

//sản phẩm trong danh mục
@Composable
fun CategoryItem(
    name: String,
    icon: ImageVector,
    bg: Color,
    tint: Color
) {
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

//trang danh mục
@Composable
fun CategoryScreen(primary: Color, initialCategory: String, onProductClick: (Product) -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val allProducts by db.productDao().getAllProducts().collectAsState(initial = emptyList())
    val categoryList = remember(allProducts) { listOf("Tất cả") + allProducts.map { it.category }.distinct() }
    var selectedCategory by remember(initialCategory) { mutableStateOf(initialCategory) }
    val filtered = if (selectedCategory == "Tất cả") allProducts else allProducts.filter { it.category == selectedCategory }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Danh mục sản phẩm", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categoryList) { cat ->
                Button(
                    onClick = { selectedCategory = cat },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedCategory == cat) primary else Color.LightGray),
                    shape = RoundedCornerShape(12.dp)
                ) { Text(cat, color = Color.White) }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))


        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filtered) { product ->
                CategoryProductCard(product, primary, onClick = { onProductClick(product) })
            }
        }
    }
}

//danh mục sản phẩm
@Composable
fun CategoryProductCard(product: Product, primary: Color, onClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isFavorite by remember { mutableStateOf(SharedPrefs.isFavorite(context, product.id)) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Ảnh bên trái
            AsyncImage(
                model = product.imageUrl.ifBlank { "https://via.placeholder.com/150" },
                contentDescription = null,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF1F5F9)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 2. Thông tin ở giữa
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                Text(text = product.category, color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = formatPrice(product.price), color = primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            // 3. Hai nút bấm
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp) // Khoảng cách giữa 2 nút
            ) {
                // Nút Yêu thích
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2))
                        .clickable {
                            SharedPrefs.toggleFavorite(context, product.id)
                            isFavorite = !isFavorite
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Nút Thêm vào giỏ
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(primary)
                        .clickable {
                            scope.launch(Dispatchers.IO) {
                                val userEmail = (context as Activity).intent.getStringExtra("USER_EMAIL") ?: ""
                                val user = AppDatabase.getDatabase(context).userDao().getUserByEmail(userEmail)
                                val uid = user?.id ?: 1
                                val db = AppDatabase.getDatabase(context).productDao()
                                val existing = db.getCartItem(uid, product.id)
                                if (existing != null) db.updateCartQuantity(existing.cartId, existing.quantity + 1)
                                else db.addToCart(CartItem(userId = uid, productId = product.id, quantity = 1))
                                withContext(Dispatchers.Main) { Toast.makeText(context, "Đã thêm vào giỏ!", Toast.LENGTH_SHORT).show() }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
//trang yêu thích
@Composable
fun FavoriteScreen(primary: Color) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val allProducts by db.productDao().getAllProducts().collectAsState(initial = emptyList())
    var refresh by remember { mutableIntStateOf(0) }
    val favoriteIds = remember(refresh) { SharedPrefs.getFavoriteIds(context) }
    val favoriteProducts = allProducts.filter { favoriteIds.contains(it.id.toString()) }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Sản phẩm yêu thích", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        if (favoriteProducts.isEmpty()) Text("Chưa có sản phẩm yêu thích", color = Color.Gray)
        else favoriteProducts.forEach { product -> FavoriteProductCard(product, primary) { SharedPrefs.removeFavorite(context, product.id); refresh++ }; Spacer(modifier = Modifier.height(12.dp)) }
    }
}

//danh sách các sản phẩm yêu thích
@Composable
fun FavoriteProductCard(product: Product, primary: Color, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = product.imageUrl.ifBlank { "https://via.placeholder.com/150" }, contentDescription = product.name, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFE0F2FE)), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(product.category, color = Color.Gray, fontSize = 13.sp)
                Text(formatPrice(product.price), color = primary, fontWeight = FontWeight.Bold)
            }

            IconButton(onClick = onRemove) { Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red) }
        }
    }
}


//trang giỏ hàng
@Composable
fun CartScreen(primary: Color, selectedIds: Set<Int>, onIdsChange: (Set<Int>) -> Unit, onCheckout: () -> Unit) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val userEmail = (context as? Activity)?.intent?.getStringExtra("USER_EMAIL") ?: ""
    var userId by remember { mutableIntStateOf(1) }
    val allProducts by db.productDao().getAllProducts().collectAsState(initial = emptyList())
    val productMap = remember(allProducts) { allProducts.associateBy { it.id } }
    LaunchedEffect(userEmail) { if (userEmail.isNotEmpty()) userId = db.userDao().getUserByEmail(userEmail)?.id ?: 1 }
    val cartItems by db.productDao().getCartFlow(userId).collectAsState(initial = emptyList())
    val validSelectedIds = remember(cartItems, selectedIds) { selectedIds.filter { id -> cartItems.any { it.productId == id } }.toSet() }
    LaunchedEffect(validSelectedIds.size) { if (validSelectedIds.size != selectedIds.size) onIdsChange(validSelectedIds) }
    //công thức tính tiền
    val totalAmountValue by remember(cartItems, validSelectedIds, allProducts) { derivedStateOf { cartItems.filter { validSelectedIds.contains(it.productId) }.sumOf { item -> (productMap[item.productId]?.price ?: 0.0) * item.quantity } } }

    val scope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 8.dp)) {
            if (cartItems.isEmpty()) Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Giỏ hàng trống", color = Color.Gray) }
            else Column {
                Text("Giỏ hàng của bạn", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                //các chức năng
                LazyColumn { items(cartItems) { item -> productMap[item.productId]?.let { CartProductItem(it, item.quantity, primary, selectedIds.contains(item.productId), { isChecked -> onIdsChange(if (isChecked) selectedIds + item.productId else selectedIds - item.productId) }, { qty -> scope.launch(Dispatchers.IO) { db.productDao().updateCartQuantity(item.cartId, qty) } }, { scope.launch(Dispatchers.IO) { db.productDao().deleteCartItem(item) } }); Spacer(modifier = Modifier.height(12.dp)) } } }
            }
        }
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Tổng thanh toán:", color = Color.Gray)
                    Text(formatPrice(totalAmountValue), color = primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onCheckout, enabled = validSelectedIds.isNotEmpty(), modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = primary)) { Text("Thanh toán (${selectedIds.size})", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun CartProductItem(product: Product, quantity: Int, primary: Color, isSelected: Boolean, onCheckedChange: (Boolean) -> Unit, onQuantityChange: (Int) -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isSelected, onCheckedChange = onCheckedChange, colors = CheckboxDefaults.colors(checkedColor = primary))
            AsyncImage(model = product.imageUrl, contentDescription = null, modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(formatPrice(product.price), color = primary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (quantity > 1) onQuantityChange(quantity - 1) }, modifier = Modifier.size(30.dp)) { Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = primary) }
                    Text("$quantity", modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                    IconButton(onClick = { onQuantityChange(quantity + 1) }, modifier = Modifier.size(30.dp)) { Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = primary) }
                }
            }
            //lấy từ CartSreen
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.LightGray) }
        }
    }
}

@Composable
fun CheckoutScreen(primary: Color, userId: Int, selectedIds: List<Int>, onNavigateToSettings: () -> Unit, onViewOrders: () -> Unit) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()
    val cartFlow by db.productDao().getCartFlow(userId).collectAsState(initial = emptyList())
    val allProducts by db.productDao().getAllProducts().collectAsState(initial = emptyList())
    val productMap = remember(allProducts) { allProducts.associateBy { it.id } }
    val cartItems = remember(cartFlow, selectedIds) { cartFlow.filter { it.productId in selectedIds } }
    val totalPrice by remember(cartItems, productMap) { derivedStateOf { cartItems.sumOf { (productMap[it.productId]?.price ?: 0.0) * it.quantity } } }
    
    var userAddress by remember { mutableStateOf("") }
    var userPhone by remember { mutableStateOf("") }
    var isOrdering by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var showInfoWarning by remember { mutableStateOf(false) }
    var warningMessage by remember { mutableStateOf("") }
    
    var paymentMethod by remember { mutableStateOf("COD") }

    val email = (context as? Activity)?.intent?.getStringExtra("USER_EMAIL") ?: ""
    LaunchedEffect(email) { withContext(Dispatchers.IO) { db.userDao().getUserByEmail(email)?.let { userAddress = it.address ?: ""; userPhone = it.phone ?: "" } } }
    
    val shippingFee = 30000.0
    val finalPrice = totalPrice + shippingFee
    
    if (showInfoWarning) { 
        AlertDialog(
            onDismissRequest = { showInfoWarning = false }, 
            title = { Text("Thông tin không hợp lệ", fontWeight = FontWeight.Bold) }, 
            text = { Text(warningMessage) }, 
            confirmButton = { Button(onClick = { showInfoWarning = false; onNavigateToSettings() }) { Text("Cập nhật ngay") } }, 
            dismissButton = { TextButton(onClick = { showInfoWarning = false }) { Text("Để sau") } }
        ) 
    }

    if (isSuccess) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(100.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text("Cảm ơn quý khách đã đặt hàng", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Đơn hàng của bạn đang được xử lý.", color = Color.Gray)
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onViewOrders,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primary)
            ) {
                Text("Đơn hàng của tôi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    } else {
        Scaffold(
            bottomBar = { 
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { 
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { 
                        Column(modifier = Modifier.weight(1f)) { 
                            Text("Tổng thanh toán", color = Color.Gray, fontSize = 13.sp)
                            Text(formatPrice(finalPrice), color = Color(0xFFE53935), fontWeight = FontWeight.Bold, fontSize = 22.sp) 
                        }
                        Button(
                            onClick = { 
                                if (isOrdering) return@Button
                                if (userAddress.isEmpty() || userAddress == "Chưa cập nhật") { warningMessage = "Vui lòng cập nhật Địa chỉ trước khi thanh toán."; showInfoWarning = true; return@Button }
                                if (!isValidVietnamesePhoneNumber(userPhone)) { warningMessage = "Số điện thoại không hợp lệ (Phải bắt đầu bằng 0 hoặc +84 và gồm 10-11 số)."; showInfoWarning = true; return@Button }
                                
                                scope.launch(Dispatchers.IO) { 
                                    isOrdering = true
                                    try { 
                                        val description = cartItems.joinToString(", ") { "${it.quantity}x ${productMap[it.productId]?.name}" }
                                        val firstItem = cartItems.firstOrNull()
                                        val emailOfShop = if (firstItem != null) productMap[firstItem.productId]?.ownerEmail ?: "admin@gmail.com" else "admin@gmail.com"
                                        
                                        db.productDao().createOrder(Order(
                                            userId = userId, 
                                            shopEmail = emailOfShop, 
                                            productDescription = description, 
                                            totalPrice = finalPrice, 
                                            status = "Pending", 
                                            note = "Thanh toán: $paymentMethod"
                                        ))
                                        db.productDao().clearCart(userId)
                                        withContext(Dispatchers.Main) { 
                                            isSuccess = true // CHUYỂN SANG MÀN HÌNH CẢM ƠN
                                        } 
                                    } catch (e: Exception) { 
                                        withContext(Dispatchers.Main) { Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show() } 
                                    } finally { isOrdering = false } 
                                } 
                            }, 
                            enabled = cartItems.isNotEmpty(), 
                            colors = ButtonDefaults.buttonColors(containerColor = primary), 
                            shape = RoundedCornerShape(14.dp), 
                            modifier = Modifier.height(50.dp)
                        ) { 
                            Text(if (isOrdering) "Đang xử lý..." else "Đặt hàng", color = Color.White, fontWeight = FontWeight.Bold) 
                        } 
                    } 
                } 
            }
        ) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(12.dp)) {
                Text("Thanh toán", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().clickable { onNavigateToSettings() }) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(primary.copy(0.12f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.LocationOn, contentDescription = null, tint = primary) }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) { Text("Địa chỉ nhận hàng", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f)); Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray) }
                            Text(userPhone.ifEmpty { "Chưa có SĐT" }, fontWeight = FontWeight.SemiBold, color = if (!isValidVietnamesePhoneNumber(userPhone)) Color.Red else Color.Black)
                            Text(userAddress.ifEmpty { "Chưa có địa chỉ" }, color = if (userAddress.isEmpty() || userAddress == "Chưa cập nhật") Color.Red else Color.Gray, lineHeight = 20.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Phương thức thanh toán", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { paymentMethod = "COD" }) {
                            RadioButton(selected = paymentMethod == "COD", onClick = { paymentMethod = "COD" })
                            Column {
                                Text("Thanh toán khi nhận hàng")
                                Text("Thanh toán tiền mặt lúc nhận hàng", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { paymentMethod = "BANK" }) {
                            RadioButton(selected = paymentMethod == "BANK", onClick = { paymentMethod = "BANK" })
                            Column {
                                Text("Chuyển khoản")
                                Text("Thanh toán qua tài khoản ngân hàng", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Sản phẩm", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        cartItems.forEach { item -> productMap[item.productId]?.let { Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { coil.compose.AsyncImage(model = it.imageUrl.ifBlank { "https://via.placeholder.com/150" }, contentDescription = null, modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0F1F5))); Spacer(modifier = Modifier.width(12.dp)); Column(modifier = Modifier.weight(1f)) { Text(it.name, fontWeight = FontWeight.SemiBold, maxLines = 2); Text(formatPrice(it.price), color = Color(0xFFE53935), fontWeight = FontWeight.Bold); Text("Số lượng: ${item.quantity}", color = Color.Gray, fontSize = 13.sp) }; Text(formatPrice(it.price * item.quantity), fontWeight = FontWeight.Bold, color = primary) } } }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Chi tiết thanh toán", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("Tạm tính", color = Color.Gray); Text(formatPrice(totalPrice)) }
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("Phí vận chuyển", color = Color.Gray); Text(formatPrice(shippingFee)) }
                        HorizontalDivider(Modifier.padding(vertical = 10.dp), thickness = 0.5.dp)
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("Thành tiền", fontWeight = FontWeight.Bold); Text(formatPrice(finalPrice), color = Color(0xFFE53935), fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                    }
                }
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }
}

//toàn bộ sản phẩm ở trang chủ
@Composable
fun ProductCard(product: Product, primary: Color, modifier: Modifier, onClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isFavorite by remember { mutableStateOf(SharedPrefs.isFavorite(context, product.id)) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 1. Ảnh sản phẩm
            AsyncImage(
                model = product.imageUrl.ifBlank { "https://via.placeholder.com/150" },
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Tên sản phẩm
            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1
            )

            // 3. Giá tiền
            Text(
                text = formatPrice(product.price),
                color = primary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Hàng nút bấm
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nút Yêu thích
                IconButton(
                    onClick = {
                        SharedPrefs.toggleFavorite(context, product.id)
                        isFavorite = !isFavorite
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .background(Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Nút Thêm vào giỏ
                IconButton(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            val userEmail = (context as Activity).intent.getStringExtra("USER_EMAIL") ?: ""
                            val user = AppDatabase.getDatabase(context).userDao().getUserByEmail(userEmail)
                            val uid = user?.id ?: 1
                            val db = AppDatabase.getDatabase(context).productDao()
                            val existing = db.getCartItem(uid, product.id)
                            if (existing != null) db.updateCartQuantity(existing.cartId, existing.quantity + 1)
                            else db.addToCart(CartItem(userId = uid, productId = product.id, quantity = 1))
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Đã thêm vào giỏ!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .background(primary, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

//chi tiết sản phẩm
@Composable
fun ProductDetailDialog(product: Product, onDismiss: () -> Unit, primary: Color) {
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { Button(onClick = onDismiss) { Text("Đóng") } }, title = { Text(product.name, fontWeight = FontWeight.Bold) }, text = { Column { AsyncImage(model = product.imageUrl.ifBlank { "https://via.placeholder.com/150" }, contentDescription = null, modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)), contentScale = androidx.compose.ui.layout.ContentScale.Crop); Spacer(Modifier.height(16.dp)); Text("Giá: ${formatPrice(product.price)}", color = primary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp); Text("Danh mục: ${product.category}", color = Color.Gray); Text("Shop: ${product.ownerEmail}", color = Color.Blue); Text(product.description, fontSize = 14.sp) } })
}

@Composable
fun BottomNavigationBar(primaryColor: Color, currentTab: String, onTabClick: (String) -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        val items = listOf(Triple("home", "Trang chủ", Icons.Default.Home), Triple("category", "Danh mục", Icons.Default.GridView), Triple("favorite", "Yêu thích", Icons.Default.Favorite), Triple("cart", "Giỏ hàng", Icons.Default.ShoppingCart), Triple("profile", "Cá nhân", Icons.Default.Person))
        items.forEach { (tab, label, icon) -> NavigationBarItem(selected = currentTab == tab, onClick = { onTabClick(tab) }, icon = { Icon(icon, null) }, label = { Text(label, fontSize = 10.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = primaryColor, selectedTextColor = primaryColor)) }
    }
}
