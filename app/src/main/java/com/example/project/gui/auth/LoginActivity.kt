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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen(
                onLoginSuccess = { user ->
                    val intent = Intent(this, com.example.project.MainActivity::class.java)

                    // Truyền dữ liệu để MainActivity biết ai đang đăng nhập
                    intent.putExtra("USER_EMAIL", user.email)
                    intent.putExtra("USER_ROLE", user.role)

                    // Thực hiện chuyển màn hình
                    startActivity(intent)

                    // Đóng Login để không quay lại được bằng nút Back
                    finish()
                },
                onRegisterClick = {
                    startActivity(Intent(this, RegisterActivity::class.java))
                }
            )
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: (com.example.project.data.entities.User) -> Unit, // Chỉ định rõ kiểu User
                onRegisterClick: () -> Unit) {
    // State lưu dữ liệu người dùng nhập vào
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // State lưu nội dung lỗi để hiển thị dưới ô nhập
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    val context = LocalContext.current
    // Lấy database từ Room để kiểm tra tài khoản đăng nhập
    val db = remember { AppDatabase.getDatabase(context) }
    // State dùng để ẩn/hiện mật khẩu
    var passwordVisible by remember { mutableStateOf(false) }
    // Tạo ImageLoader để hiển thị ảnh GIF
    val imageLoader = ImageLoader.Builder(context)
        .components {
            add(GifDecoder.Factory())
        }
        .build()

    fun validateLogin(): Boolean {
        var isValid = true
        emailError = ""
        passwordError = ""

        if (email.trim().isEmpty()) {
            emailError = "Vui lòng nhập email"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            emailError = "Email không đúng định dạng"
            isValid = false
        }

        if (password.trim().isEmpty()) {
            passwordError = "Vui lòng nhập mật khẩu"
            isValid = false
        }
        return isValid
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Đăng nhập",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

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

        OutlinedTextField(
            value = email,
            onValueChange = {
                // bỏ khoảng trắng và giới hạn tối đa 50 ký tự
                email = it.replace(" ", "").take(50)
                emailError = ""
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = emailError.isNotEmpty(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        if (emailError.isNotEmpty()) {
            Text(
                text = emailError,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                // giới hạn tối đa 20 ký tự
                password = it.take(20)
                passwordError = ""
            },
            label = { Text("Mật khẩu") },
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = passwordError.isNotEmpty(),
            trailingIcon = {
                val image = if (passwordVisible) {
                    Icons.Filled.Visibility
                } else {
                    Icons.Filled.VisibilityOff
                }

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

        if (passwordError.isNotEmpty()) {
            Text(
                text = passwordError,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (!validateLogin()) return@Button

                CoroutineScope(Dispatchers.IO).launch {
                    val user = db.userDao().login(email.trim(), password.trim())
                    withContext(Dispatchers.Main) {
                        if (user != null) {
                            onLoginSuccess(user)
                        } else {
                            Toast.makeText(context, "Email hoặc mật khẩu không chính xác", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48C25)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Đăng nhập",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        TextButton(onClick = onRegisterClick) {
            Text(
                "Chưa có tài khoản? Đăng ký ngay",
                color = Color(0xFFF48C25),
                fontWeight = FontWeight.Bold)
        }
    }
}
