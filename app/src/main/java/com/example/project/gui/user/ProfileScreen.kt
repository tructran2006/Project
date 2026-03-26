package com.example.project.gui.user

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.* // Thêm remember, useState, ...
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// --- IMPORT COIL ---
import coil.compose.AsyncImage
// --- IMPORT ENTITIES VÀ DAO ---
import com.example.project.data.database.AppDatabase
import com.example.project.data.entities.User
import com.example.project.gui.admin.AdminActivity
import com.example.project.gui.auth.LoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(
    db: AppDatabase,
    name: String,
    email: String,
    userRole: String,
    onNavigateToSettings: () -> Unit,
    onNavigateToFavorite: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    val primaryColor = Color(0xFFF48C25)
    val bgColor = Color(0xFFF8F7F5)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State quản lý Dialog dán link ảnh
    var showAvatarDialog by remember { mutableStateOf(false) }
    var avatarUrlInput by remember { mutableStateOf("") }

    // State lưu ảnh đại diện hiện tại để hiển thị (Load từ DB)
    var currentAvatarUri by remember { mutableStateOf<String?>(null) }

    // Load ảnh đại diện khi màn hình được tạo ra
    LaunchedEffect(email) {
        withContext(Dispatchers.IO) {
            val user = db.userDao().getUserByEmail(email)
            withContext(Dispatchers.Main) {
                currentAvatarUri = user?.avatarUri
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. Header & Avatar (ĐÃ CẬP NHẬT) ---
        Spacer(modifier = Modifier.height(40.dp))

        // Box chứa ảnh và nút sửa ảnh
        Box(
            modifier = Modifier.size(120.dp), // Tăng nhẹ kích thước Box để nút không bị cắt
            contentAlignment = Alignment.Center // Ảnh luôn nằm giữa
        ) {
            // 1. Khung chứa ảnh đại diện
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, primaryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!currentAvatarUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = currentAvatarUri,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(70.dp),
                        tint = primaryColor
                    )
                }
            }

            // 2. NÚT SỬA ẢNH (Đưa xuống góc dưới bên phải)
            IconButton(
                onClick = {
                    avatarUrlInput = currentAvatarUri ?: ""
                    showAvatarDialog = true
                },
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.BottomEnd) // ĐẨY XUỐNG GÓC DƯỚI
                    .background(primaryColor, CircleShape)
                    .border(2.dp, Color.White, CircleShape) // Thêm viền trắng cho nút nổi bật
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = "Change Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C140D)
        )
        Text(
            text = email,
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- Menu List (Giữ nguyên của bạn) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(vertical = 8.dp)
        ) {
            if (userRole == "shop" || userRole == "admin") {
                ProfileMenuItem(
                    icon = Icons.Default.Storefront,
                    title = if (userRole == "admin") "Bảng điều khiển Admin" else "Quản lý cửa hàng",
                    onClick = {
                        val intent = Intent(context, AdminActivity::class.java).apply {
                            putExtra("USER_EMAIL", email)
                            putExtra("USER_ROLE", userRole)
                        }
                        context.startActivity(intent)
                    }
                )
                HorizontalDivider(color = bgColor, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
            }

            ProfileMenuItem(
                icon = Icons.Default.Favorite,
                title = "Danh sách yêu thích",
                onClick = onNavigateToFavorite
            )

            // Sửa lại onClick của Đơn hàng:
            ProfileMenuItem(
                icon = Icons.Default.ShoppingBag,
                title = "Đơn hàng của tôi",
                onClick = onNavigateToOrders
            )
            ProfileMenuItem(
                icon = Icons.Default.Settings,
                title = "Cài đặt tài khoản",
                onClick = onNavigateToSettings
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- Nút Đăng xuất (Giữ nguyên) ---
        Button(
            onClick = {
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Logout, null, tint = Color.Red)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Đăng xuất", color = Color.Red, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(20.dp))
    }

    // --- DIALOG DÁN LINK ẢNH ĐẠI DIỆN ---
    if (showAvatarDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            title = { Text("Cập nhật ảnh đại diện", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Dán link ảnh (URL) vào ô bên dưới:", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = avatarUrlInput,
                        onValueChange = { avatarUrlInput = it },
                        placeholder = { Text("https://example.com/image.jpg") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    // Logic cập nhật vào Database
                    scope.launch(Dispatchers.IO) {
                        // Gọi hàm updateAvatar trong Dao
                        db.userDao().updateAvatar(email, avatarUrlInput.trim())

                        // Cập nhật lại giao diện ngay lập tức
                        withContext(Dispatchers.Main) {
                            currentAvatarUri = avatarUrlInput.trim()
                            showAvatarDialog = false
                            Toast.makeText(context, "Đã cập nhật ảnh!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text("Cập nhật")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAvatarDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

// Hàm ProfileMenuItem giữ nguyên của bạn
@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color(0xFF9C7349), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}