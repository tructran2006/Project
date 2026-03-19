package com.example.project.gui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project.MainActivity
import com.example.project.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.imageLoader
import coil.request.ImageRequest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.project.gui.shop.AdminActivity


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen(
                onLoginSuccess = { user: com.example.project.data.entities.User ->
                    if (user.role == "admin") {
                        val intent = Intent(this@LoginActivity, AdminActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, MainActivity::class.java)
                        // GỬI EMAIL SANG MAIN ĐỂ HIỂN THỊ PROFILE
                        intent.putExtra("USER_EMAIL", user.email)
                        startActivity(intent)
                    }
                    finish()
                },
                onRegisterClick = {
                    val intent = Intent(this, RegisterActivity::class.java)
                    startActivity(intent)
                }
            )
        }
    }
    }

    @Composable
    fun LoginScreen(onLoginSuccess: (com.example.project.data.entities.User) -> Unit, // Chỉ định rõ kiểu User
                    onRegisterClick: () -> Unit) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        val context = androidx.compose.ui.platform.LocalContext.current
        val db = remember { AppDatabase.getDatabase(context) }
        var passwordVisible by remember { mutableStateOf(false) }
        val imageLoader = ImageLoader.Builder(context)
            .components {
                add(GifDecoder.Factory())
            }
            .build()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Đăng nhập",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Gif welcome (Banner)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFFEE2E2), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(context)
                            .data("https://media4.giphy.com/media/v1.Y2lkPTc5MGI3NjExeDNuZzc0MmVhOTBkdG85MXhmMms5bDgwdnI4OXMybHQxNXVvZXB0cCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/TzXweXeJ9d1IhZ5YAy/giphy.gif")
                            .build(),
                        imageLoader = imageLoader
                    ),
                    contentDescription = "Shopping GIF",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "3TP SHOP!",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )

            Text(
                text = "Vui lòng đăng nhập để tiếp tục khám phá.",
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Input Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mật khẩu") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else
                        Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = image,
                            contentDescription = null,
                            tint = Color(0xFF64748B)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Nút Đăng nhập (Màu cam Primary)
            // Nút Đăng nhập
            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Vui lòng nhập đủ!", Toast.LENGTH_SHORT).show()
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            val user = db.userDao().login(email.trim(), password.trim())
                            withContext(Dispatchers.Main) {
                                if (user != null) {
                                    // TRUYỀN NGUYÊN ĐỐI TƯỢNG USER LÊN ONCREAET
                                    onLoginSuccess(user)
                                } else {
                                    Toast.makeText(context, "Sai tài khoản!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48C25)),
                shape = RoundedCornerShape(12.dp)
            )
            {
                Text(text = "Đăng nhập", color = Color.White, fontWeight = FontWeight.Bold)
            }

            // Link Đăng ký
            TextButton(onClick = onRegisterClick) {
                Text("Chưa có tài khoản? Đăng ký ngay", color = Color(0xFFF48C25), fontWeight = FontWeight.Bold)
            }
        }
    }
