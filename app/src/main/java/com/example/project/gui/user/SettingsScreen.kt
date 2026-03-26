package com.example.project.gui.user

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project.data.database.AppDatabase
import com.example.project.gui.admin.AdminActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

@Composable
fun SettingsScreen(
    userName: String,
    userEmail: String,
    userRole: String,
    userBirthday: String,
    userAge: String,
    userAddress: String,
    onBack: () -> Unit,
    onUpdateSuccess: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val primaryColor = Color(0xFFF48C25)
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    // Khởi tạo state chỉnh sửa từ dữ liệu hiện có
    var editName by remember { mutableStateOf(userName) }
    var editBirthday by remember { mutableStateOf(userBirthday) }
    var editAge by remember { mutableStateOf(userAge) }
    var editAddress by remember { mutableStateOf(userAddress) }
    var editPhone by remember { mutableStateOf("") }


    var userBirthdayState by remember { mutableStateOf(userBirthday) }
    val calendar = Calendar.getInstance()
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
            Text("Cài đặt tài khoản", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card Hiển thị thông tin
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    InfoRow("Họ và tên", userName)
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                    InfoRow("Email", userEmail)
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                    InfoRow("Ngày sinh", userBirthday)
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
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
                Text("Cập nhật thông tin", color = primaryColor, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- NÚT TRỞ THÀNH SHOP (CẬP NHẬT LOGIC) ---
            if (userRole != "shop" && userRole != "admin") {
                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            db.userDao().updateUserRole(userEmail, "shop")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Kích hoạt Kênh Người Bán thành công!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(context, AdminActivity::class.java).apply {
                                    putExtra("USER_EMAIL", userEmail)
                                    putExtra("USER_ROLE", "shop")
                                }
                                context.startActivity(intent)
                                onUpdateSuccess()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mở cửa hàng của bạn", fontWeight = FontWeight.Bold)
                }
            } else if (userRole == "shop") {
                // GỢI Ý: Nếu đã là Shop, hiện nút để vào nhanh trang quản lý
                OutlinedButton(
                    onClick = {
                        val intent = Intent(context, AdminActivity::class.java).apply {
                            putExtra("USER_EMAIL", userEmail)
                            putExtra("USER_ROLE", "shop")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Vào trang quản lý Shop", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // --- Dialog Chỉnh sửa ---
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Chỉnh sửa hồ sơ") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Tên hiển thị") })
                    OutlinedTextField(
                        value = userBirthdayState,
                        onValueChange = { },
                        label = { Text("Ngày sinh") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                val datePickerDialog = android.app.DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        userBirthdayState = "$day/${month + 1}/$year"
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                )
                                datePickerDialog.show()
                            }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Chọn ngày sinh")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(value = editAddress, onValueChange = { editAddress = it }, label = { Text("Địa chỉ nhận hàng") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch(Dispatchers.IO) {
                        db.userDao().updateUserInfo(
                            email = userEmail,
                            name = editName,
                            birthday = editBirthday,
                            age = editAge.toIntOrNull() ?: 0,
                            address = editAddress,
                            phone = editPhone
                        )
                        withContext(Dispatchers.Main) {
                            showDialog = false
                            onUpdateSuccess()
                            Toast.makeText(context, "Đã lưu thay đổi", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) { Text("Lưu thay đổi") }
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