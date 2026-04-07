package com.example.project.gui.auth

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project.data.database.AppDatabase
import com.example.project.data.entities.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegisterScreen()
        }
    }

    @Composable
    fun RegisterScreen() {
        val context = LocalContext.current
        val db = remember { AppDatabase.getDatabase(context) }

        // State cho các ô nhập liệu
        var fullName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var confirmpassword by remember { mutableStateOf("") }
        var confirmPasswordVisible by remember { mutableStateOf(false) }
        var fullNameError by remember { mutableStateOf("") }
        var emailError by remember { mutableStateOf("") }
        var passwordError by remember { mutableStateOf("") }
        var confirmPasswordError by remember { mutableStateOf("") }


        // State cho hiệu ứng gõ chữ
        val titleText = "3TP SHOP - ĐĂNG KÝ"
        var displayedTitle by remember { mutableStateOf("") }
        LaunchedEffect(Unit) {
            for (char in titleText) {
                displayedTitle += char
                delay(100)
            }
        }

        // Hàm kiểm tra dữ liệu trước khi đăng ký
        // Nếu dữ liệu sai thì hiển thị lỗi dưới ô nhập
        fun validateRegister(): Boolean {
            var isValid = true
            fullNameError = ""
            emailError = ""
            passwordError = ""
            confirmPasswordError = ""

            if (fullName.trim().isEmpty()) {
                fullNameError = "Vui lòng nhập họ tên"
                isValid = false
            }

            if (email.trim().isEmpty()) {
                emailError = "Vui lòng nhập email"
                isValid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                emailError = "Email không đúng định dạng"
                isValid = false
            }

            if (password.trim().length < 6) {
                passwordError = "Mật khẩu phải từ 6 ký tự"
                isValid = false
            }

            if (confirmpassword.trim() != password.trim()) {
                confirmPasswordError = "Mật khẩu xác nhận không khớp"
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
            Spacer(modifier = Modifier.height(40.dp))

            // Tiêu đề với hiệu ứng gõ chữ
            Text(
                // gọi hiệu ứng ở trên
                text = displayedTitle,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFF48C25) // Màu cam thương hiệu
            )

            Text(
                text = "Tạo tài khoản mới để bắt đầu mua sắm.",
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )


            // Ô nhập họ và tên
            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    // không cho nhập ký tự đặc biệt (chỉ cho chữ + khoảng trắng)
                    val filtered = it.filter { char ->
                        char.isLetter() || char.isWhitespace()
                    }

                    // giới hạn tối đa 30 ký tự
                    fullName = filtered.take(30)

                    fullNameError = ""
                },
                label = { Text("Họ và tên") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = fullNameError.isNotEmpty()
            )

            // Hiển thị lỗi nếu có
            if (fullNameError.isNotEmpty()) {
                Text(
                    text = fullNameError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ô nhập Email
            // Ô nhập email
            OutlinedTextField(
                value = email,
                onValueChange = {
                    // không cho space
                    val filtered = it.replace(" ", "")
                    email = filtered.take(50)
                    emailError = ""
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = emailError.isNotEmpty(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            // Hiển thị lỗi nếu có
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

            // Ô nhập Mật khẩu
            OutlinedTextField(
                value = password,
                onValueChange = {
                    // giới hạn tối đa 20 ký tự
                    password = it.take(20)
                    passwordError = ""
                },
                label = { Text("Mật khẩu") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = Color(0xFF64748B))
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Ô xác nhận mật khẩu
            OutlinedTextField(
                value = confirmpassword,
                onValueChange = {
                    confirmpassword = it.take(20)
                    confirmPasswordError = ""
                },
                label = { Text("Xác nhận mật khẩu") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = confirmPasswordError.isNotEmpty(),
                visualTransformation = if (confirmPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = image,
                            contentDescription = null,
                            tint = Color(0xFF64748B)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            if (confirmPasswordError.isNotEmpty()) {
                Text(
                    text = confirmPasswordError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 4.dp)
                )
            }

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
            Spacer(modifier = Modifier.height(32.dp))

            // Nút Đăng ký
            Button(
                onClick = {
                    if (!validateRegister()) return@Button

                    CoroutineScope(Dispatchers.IO).launch {
                        val existingUser = db.userDao().getUserByEmail(email.trim())
                        if (existingUser != null) {
                            withContext(Dispatchers.Main) { emailError = "Email này đã được đăng ký" }
                            return@launch
                        }

                        val newUser = User(
                            username = fullName.trim(),
                            email = email.trim(),
                            password = password.trim(),
                            role = "user"
                        )
                        db.userDao().register(newUser)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                            (context as? Activity)?.finish()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48C25)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Đăng ký", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quay lại Đăng nhập
            TextButton(onClick = { finish() }) {
                Text("Đã có tài khoản? Đăng nhập ngay", color = Color(0xFFF48C25))
            }
        }
    }
}