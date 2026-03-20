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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.project.gui.auth.LoginActivity // Import đúng đường dẫn Login của bạn
import com.google.android.gms.cast.framework.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(db: AppDatabase,name: String, email: String,onNavigateToSettings: () -> Unit) {
    val primaryColor = Color(0xFFF48C25)
    val bgColor = Color(0xFFF8F7F5)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Header & Avatar
        Spacer(modifier = Modifier.height(40.dp))
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, primaryColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(70.dp),
                tint = primaryColor
            )
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


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(vertical = 8.dp)
        ) {
            ProfileMenuItem(Icons.Default.ShoppingBag, "Đơn hàng của tôi")
            HorizontalDivider(color = bgColor, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
            ProfileMenuItem(Icons.Default.Favorite, "Danh sách yêu thích")
            HorizontalDivider(color = bgColor, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
            ProfileMenuItem(
                icon = Icons.Default.Settings,
                title = "Cài đặt tài khoản",
                onClick = onNavigateToSettings)
        }

        Spacer(modifier = Modifier.weight(1f)) // Đẩy nút đăng xuất xuống gần cuối

        Button(
            onClick = {
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Đăng xuất", color = Color.Red, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit = {} // Đảm bảo có tham số này
) {
    // Sử dụng Surface bọc ngoài để tạo hiệu ứng bấm và nhận sự kiện onClick
    Surface(
        onClick = onClick,
        color = Color.Transparent, // Để không đè màu lên Row
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
