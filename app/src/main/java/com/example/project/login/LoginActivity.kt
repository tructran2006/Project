package com.example.project.login

import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project.login.ui.theme.*
import androidx.compose.material3.ExperimentalMaterial3Api // Nếu dùng API thử nghiệm
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.filled.* // Hoặc import cụ thể từng cái
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.imageLoader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                LoginScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LoginScreen() {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        val context = LocalContext.current
        val imageLoader = remember {
            ImageLoader.Builder(context)
                .components {
                    if (SDK_INT >= 28) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                }
                .crossfade(true) // Thêm hiệu ứng hiện hình mượt mà
                .build()
        }

        Box(modifier = Modifier.fillMaxSize().background(White)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp)
            ) {

                // --- ILLUSTRATION ---
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Slate50),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = "https://media0.giphy.com/media/v1.Y2lkPTc5MGI3NjExbXQwbnNuMDRvamthbDY5Yml5cWk2d294ZTN6ZndmYm93b2ZudXBlMyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/onySZTp1wrSa9wVWc7/giphy.gif",
                        contentDescription = "Login Animation",
                        imageLoader = imageLoader,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        // Hiện màu xám khi đang tải
                        placeholder = ColorPainter(Color.LightGray),
                        // Hiện màu đỏ nếu link chết hoặc không có mạng
                        error = ColorPainter(Color.Red)
                    )
                }

                // --- WELCOME TEXT ---
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Chào mừng trở lại!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Vui lòng đăng nhập để tiếp tục khám phá những ưu đãi mới nhất.",
                        textAlign = TextAlign.Center,
                        color = Slate600,
                        modifier = Modifier.padding(top = 8.dp),
                        lineHeight = 22.sp
                    )
                }

                // --- FORM FIELDS ---
                Column(modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()) {
                    Text(
                        "Email",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nhập email của bạn") },
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Slate50,
                            unfocusedContainerColor = Slate50,
                            focusedIndicatorColor = Primary,
                            unfocusedIndicatorColor = Slate200,
                            disabledIndicatorColor = Slate200,
                            cursorColor = Primary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Mật khẩu",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("••••••••") },
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        trailingIcon = {
                            Icon(
                                Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Slate400
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Slate50,
                            unfocusedContainerColor = Slate50,
                            focusedIndicatorColor = Primary,
                            unfocusedIndicatorColor = Slate200,
                            disabledIndicatorColor = Slate200,
                            cursorColor = Primary
                        ),
                        singleLine = true
                    )
                    Text(
                        text = "Quên mật khẩu?",
                        color = Primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.End).padding(top = 8.dp).clickable { }
                    )
                }

                // --- LOGIN BUTTON ---
                Button(
                    onClick = { /* Login Logic */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Đăng nhập", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                // --- SOCIAL DIVIDER ---
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(modifier = Modifier.weight(1f), color = Slate200)
                    Text(
                        "HOẶC ĐĂNG NHẬP VỚI",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate500
                    )
                    Divider(modifier = Modifier.weight(1f), color = Slate200)
                }

                // --- SOCIAL BUTTONS ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    SocialCircleButton(color = Color.White) // Google
                    Spacer(modifier = Modifier.width(24.dp))
                    SocialCircleButton(color = Color.White) // Apple
                    Spacer(modifier = Modifier.width(24.dp))
                    SocialCircleButton(color = Color.White) // Facebook
                }

                Spacer(modifier = Modifier.weight(1f))

                // --- FOOTER ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Chưa có tài khoản?", color = Slate500)
                    Text(
                        " Đăng ký ngay",
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { }
                    )
                }
            }
        }
    }

    @Composable
    fun SocialCircleButton(color: Color) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .border(1.dp, Slate200, CircleShape)
                .clip(CircleShape)
                .background(color)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            // Thay bằng icon thực tế (painterResource)
            Box(modifier = Modifier.size(24.dp).background(Slate200, CircleShape))
        }
    }
}