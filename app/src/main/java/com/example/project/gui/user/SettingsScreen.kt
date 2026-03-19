package com.example.project.gui.user

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project.data.database.AppDatabase
import com.example.project.gui.shop.AdminActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    userName: String,
    userEmail: String,
    userBirthday: String,
    userAge: String,
    userAddress: String,
    onBack: () -> Unit,
    onUpdateSuccess: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val primaryColor = Color(0xFFF48C25)

    var showDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(userName) }
    var editBirthday by remember { mutableStateOf(userBirthday) }
    var editAge by remember { mutableStateOf(userAge) }
    var editAddress by remember { mutableStateOf(userAddress) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7F5))
    ) {
        // --- 1. Top Bar ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
            }
            Text("Thông tin cá nhân", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        // --- 2. Nội dung chính ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card Thông tin
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    InfoRow("Họ và tên", userName)
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray)
                    InfoRow("Email", userEmail)
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray)
                    InfoRow("Ngày sinh", userBirthday)
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray)
                    InfoRow("Tuổi", userAge)
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray)
                    InfoRow("Địa chỉ", userAddress)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nút Chỉnh sửa
            OutlinedButton(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, primaryColor)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = primaryColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chỉnh sửa thông tin", color = primaryColor, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nút Trở thành Shop (Đã quay trở lại)
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        db.userDao().updateUserRole(userEmail, "admin")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Chúc mừng! Bạn đã trở thành Shop", Toast.LENGTH_SHORT).show()

                            // MỞ TRANG ADMIN NGAY LẬP TỨC
                            val intent = Intent(context, AdminActivity::class.java)
                            context.startActivity(intent)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Storefront, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Trở thành người bán hàng", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // --- 3. Dialog (Nằm ngoài Column nhưng trong hàm SettingsScreen) ---
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Chỉnh sửa thông tin", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Họ tên") })
                    OutlinedTextField(value = editBirthday, onValueChange = { editBirthday = it }, label = { Text("Ngày sinh") })
                    OutlinedTextField(value = editAge, onValueChange = { editAge = it }, label = { Text("Tuổi") })
                    OutlinedTextField(value = editAddress, onValueChange = { editAddress = it }, label = { Text("Địa chỉ") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            db.userDao().updateUserInfo(
                                email = userEmail,
                                name = editName,
                                birthday = editBirthday,
                                age = editAge.toIntOrNull() ?: 0,
                                address = editAddress
                            )
                            withContext(Dispatchers.Main) {
                                showDialog = false
                                onUpdateSuccess()
                                Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Lưu")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Hủy") }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1C140D))
    }
}