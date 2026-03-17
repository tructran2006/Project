package com.example.project

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.project.data.database.AppDatabase
import com.example.project.gui.user.ProfileScreen
import com.example.project.gui.user.SettingsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    val scrollState = rememberScrollState()
    val primaryColor = Color(0xFFF48C25)
    val bgColor = Color(0xFFF8F7F5)
    val db = remember { AppDatabase.getDatabase(context) }

    // Quản lý tab đang chọn
    var currentTab by remember { mutableStateOf("home") }

    // State lưu thông tin User lấy từ DB
    var userName by remember { mutableStateOf("Đang tải...") }
    var userEmail by remember { mutableStateOf("...") }

    //thông tin ngày tháng năm , địa chỉ
    var userBirthday by remember { mutableStateOf("") }
    var userAge by remember { mutableStateOf("") }
    var userAddress by remember { mutableStateOf("") }

    // Lấy Email gửi từ LoginActivity sang
    val emailFromIntent = (context as? Activity)?.intent?.getStringExtra("USER_EMAIL") ?: ""

    // Hiệu ứng lấy dữ liệu khi mở app
    LaunchedEffect(Unit) {
        if (emailFromIntent.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                val user = db.userDao().getUserByEmail(emailFromIntent)
                user?.let {
                    userName = it.username
                    userEmail = it.email
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (emailFromIntent.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                val user = db.userDao().getUserByEmail(emailFromIntent)
                user?.let {
                    userName = it.username
                    userEmail = it.email
                    userBirthday = it.birthday ?: "Chưa cập nhật"
                    userAge = it.age?.toString() ?: "0"
                    userAddress = it.address ?: "Chưa cập nhật"
                }
            }
        }
    }
    var refreshTrigger by remember { mutableIntStateOf(0) } // Dùng biến này để kích hoạt load lại

    LaunchedEffect(emailFromIntent, refreshTrigger) { // Load lại mỗi khi refreshTrigger thay đổi
        if (emailFromIntent.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                val user = db.userDao().getUserByEmail(emailFromIntent)
                user?.let {
                    userName = it.username
                    userEmail = it.email
                    userBirthday = it.birthday ?: "Chưa cập nhật"
                    userAge = it.age?.toString() ?: "0"
                    userAddress = it.address ?: "Chưa cập nhật"
                }
            }
        }
    }
// Khi gọi SettingsScreen
    SettingsScreen(
        userName = userName,
        userEmail = userEmail,
        userBirthday = userBirthday,
        userAge = userAge,
        userAddress = userAddress,
        onBack = { currentTab = "profile" },
        onUpdateSuccess = { refreshTrigger++ }
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                primaryColor = primaryColor,
                currentTab = currentTab,
                onTabClick = { tabName -> currentTab = tabName }
            )
        },
        containerColor = bgColor
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (currentTab) {
                "home" -> {
                    // TRANG CHỦ
                    Column(
                        modifier = Modifier
                            .verticalScroll(scrollState)
                            .fillMaxSize()
                    ) {
                        HeaderSection(primaryColor)
                        SearchBarSection()
                        PromoCarousel()
                        CategorySection(primaryColor)
                        FlashSaleSection(primaryColor)
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                "profile" -> {
                    ProfileScreen(
                        name = userName,
                        email = userEmail,
                        onNavigateToSettings = {
                            currentTab = "settings" // Lệnh đổi tab
                        }
                    )
                }
                "settings" -> {
                    SettingsScreen(
                        userName = userName,
                        userEmail = userEmail,
                        userBirthday = userBirthday,
                        userAge = userAge,
                        userAddress = userAddress,
                        onBack = { currentTab = "profile" },
                        onUpdateSuccess = {
                            refreshTrigger++
                        }
                    )
                }
            }
        }
    }
}
    @Composable
    fun HeaderSection(primary: Color) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Chào buổi sáng 👋", color = Color(0xFF9C7349), fontSize = 12.sp)
                Text("Khám phá ngay", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            //fix
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BadgedBox(badge = { Badge(containerColor = primary) }) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }


    //fix
    @Composable
    fun SearchBarSection() {
        var text by remember { mutableStateOf("") }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Tìm kiếm sản phẩm...", color = Color(0xFF9C7349)) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF9C7349)
                )
            },
            trailingIcon = {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = null,
                    tint = Color(0xFFF48C25)
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
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }

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
    fun CategorySection(primary: Color) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Danh mục", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Xem tất cả", color = primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            LazyRow(
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    CategoryItem(
                        "Công nghệ",
                        Icons.Default.Devices,
                        Color(0xFFFFEDD5),
                        primary
                    )
                }
                item {
                    CategoryItem(
                        "Thời trang",
                        Icons.Default.Checkroom,
                        Color(0xFFE0F2FE),
                        Color(0xFF3B82F6)
                    )
                }
                item {
                    CategoryItem(
                        "Làm đẹp",
                        Icons.Default.Face,
                        Color(0xFFFEF2F2),
                        Color(0xFFEF4444)
                    )
                }
                item {
                    CategoryItem(
                        "Giải trí",
                        Icons.Default.SportsEsports,
                        Color(0xFFFAF5FF),
                        Color(0xFFA855F7)
                    )
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
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(32.dp))
            }
            Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }

    @Composable
    fun FlashSaleSection(primary: Color) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Flash Sale", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Box(
                    Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFFEE2E2))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "02:14:55",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ProductCard("Tai nghe S24", "950.000đ", primary, Modifier.weight(1f))
                ProductCard("Smartwatch X", "2.100.000đ", primary, Modifier.weight(1f))
            }
        }
    }

    @Composable
    fun ProductCard(name: String, price: String, primary: Color, modifier: Modifier) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp)).background(Color(0xFFE0F2FE))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(price, color = primary, fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = {},
                        modifier = Modifier.size(32.dp).background(primary, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    }

    @Composable
    fun BottomNavigationBar(
        primaryColor: Color,
        currentTab: String,
        onTabClick: (String) -> Unit
    ) {
        NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
            NavigationBarItem(
                selected = currentTab == "home",
                onClick = { onTabClick("home") },
                icon = { Icon(Icons.Default.Home, null) },
                label = { Text("Trang chủ") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = primaryColor,
                    selectedTextColor = primaryColor
                )
            )
            NavigationBarItem(
                selected = currentTab == "category",
                onClick = { onTabClick("category") },
                icon = { Icon(Icons.Default.GridView, null) },
                label = { Text("Danh mục") }
            )
            NavigationBarItem(
                selected = currentTab == "favorite",
                onClick = { onTabClick("favorite") },
                icon = { Icon(Icons.Default.Favorite, null) },
                label = { Text("Yêu thích") }
            )
            NavigationBarItem(
                selected = currentTab == "profile",
                onClick = { onTabClick("profile") },
                icon = { Icon(Icons.Default.Person, null) },
                label = { Text("Cá nhân") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = primaryColor,
                    selectedTextColor = primaryColor
                )
            )
        }
    }
