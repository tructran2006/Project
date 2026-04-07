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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project.data.database.AppDatabase
import com.example.project.gui.admin.AdminActivity
import com.example.project.utils.AddressData
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
    userPhone: String,
    onBack: () -> Unit,
    onUpdateSuccess: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val primaryColor = Color(0xFFF48C25)
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    
    // State quản lý thông tin chỉnh sửa trong Dialog
    var editName by remember { mutableStateOf(userName) }
    var editPhone by remember { mutableStateOf(userPhone) }
    var userBirthdayState by remember { mutableStateOf(userBirthday) }

    // --- Tách chuỗi địa chỉ từ DB để hiển thị lên UI chọn ---
    val addressParts = userAddress.split(", ").map { it.trim() }
    var selectedProvince by remember { mutableStateOf(addressParts.getOrNull(3) ?: "") }
    var selectedDistrict by remember { mutableStateOf(addressParts.getOrNull(2) ?: "") }
    var selectedWard by remember { mutableStateOf(addressParts.getOrNull(1) ?: "") }
    var detailAddress by remember { mutableStateOf(addressParts.getOrNull(0) ?: "") }

    val calendar = Calendar.getInstance()

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF8F7F5))
    ) {
        // --- 1. Top Bar ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Quay lại") }
            Text("Cài đặt tài khoản", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card Hiển thị thông tin hiện tại
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    InfoRow("Họ và tên", userName)
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), 0.5.dp)
                    InfoRow("Email", userEmail)
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), 0.5.dp)
                    InfoRow("Số điện thoại", if(userPhone.isEmpty()) "Chưa cập nhật" else userPhone)
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), 0.5.dp)
                    InfoRow("Ngày sinh", userBirthday)
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), 0.5.dp)
                    InfoRow("Địa chỉ", if(userAddress.isEmpty() || userAddress == "Chưa cập nhật") "Chưa cập nhật" else userAddress)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nút mở Dialog cập nhật thông tin
            OutlinedButton(
                onClick = { 
                    editName = userName
                    editPhone = userPhone
                    userBirthdayState = userBirthday
                    // Reset lại địa chỉ chi tiết theo dữ liệu cũ
                    val parts = userAddress.split(", ").map { it.trim() }
                    selectedProvince = parts.getOrNull(3) ?: ""
                    selectedDistrict = parts.getOrNull(2) ?: ""
                    selectedWard = parts.getOrNull(1) ?: ""
                    detailAddress = parts.getOrNull(0) ?: ""
                    showDialog = true 
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, primaryColor)
            ) {
                Icon(Icons.Default.Edit, null, tint = primaryColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cập nhật thông tin", color = primaryColor, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Nút TRỞ THÀNH SHOP (Nếu chưa là Shop) ---
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
                    Icon(Icons.Default.Storefront, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mở cửa hàng của bạn", fontWeight = FontWeight.Bold)
                }
            } else if (userRole == "shop") {
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

    // --- DIALOG CHỈNH SỬA THÔNG TIN ---
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Chỉnh sửa hồ sơ", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Thông tin cơ bản
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Tên hiển thị") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Số điện thoại") }, modifier = Modifier.fillMaxWidth())
                    
                    OutlinedTextField(
                        value = userBirthdayState,
                        onValueChange = { },
                        label = { Text("Ngày sinh") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                android.app.DatePickerDialog(context, { _, y, m, d -> userBirthdayState = "$d/${m+1}/$y" },
                                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                            }) { Icon(Icons.Default.CalendarToday, null) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // --- CHỌN ĐỊA CHỈ PHÂN CẤP ---
                    Text("Địa chỉ nhận hàng", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = primaryColor)
                    
                    // 1. Tỉnh/Thành phố
                    AddressDropdown(
                        label = "Tỉnh/Thành phố",
                        selectedOption = selectedProvince,
                        options = AddressData.provinces,
                        onOptionSelected = {
                            selectedProvince = it
                            selectedDistrict = ""
                            selectedWard = ""
                        }
                    )

                    // 2. Quận/Huyện
                    AddressDropdown(
                        label = "Quận/Huyện",
                        selectedOption = selectedDistrict,
                        options = if(selectedProvince.isNotEmpty()) AddressData.districts[selectedProvince] ?: emptyList() else emptyList(),
                        onOptionSelected = {
                            selectedDistrict = it
                            selectedWard = ""
                        },
                        enabled = selectedProvince.isNotEmpty()
                    )

                    // 3. Phường/Xã
                    AddressDropdown(
                        label = "Phường/Xã",
                        selectedOption = selectedWard,
                        options = if(selectedDistrict.isNotEmpty()) AddressData.getWardsForDistrict(selectedDistrict) else emptyList(),
                        onOptionSelected = { selectedWard = it },
                        enabled = selectedDistrict.isNotEmpty()
                    )

                    // 4. Địa chỉ chi tiết (Số nhà, đường)
                    OutlinedTextField(
                        value = detailAddress,
                        onValueChange = { detailAddress = it },
                        label = { Text("Số nhà, tên đường...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    // Ghép các phần thành chuỗi chuẩn: "Số nhà, Phường, Quận, Tỉnh"
                    val fullAddress = if(selectedProvince.isNotEmpty()) {
                        "$detailAddress, $selectedWard, $selectedDistrict, $selectedProvince"
                    } else "Chưa cập nhật"

                    scope.launch(Dispatchers.IO) {
                        db.userDao().updateUserInfo(
                            email = userEmail,
                            name = editName,
                            birthday = userBirthdayState,
                            age = 0,
                            address = fullAddress,
                            phone = editPhone
                        )
                        withContext(Dispatchers.Main) {
                            showDialog = false
                            onUpdateSuccess() // Báo hiệu để MainActivity cập nhật lại UI
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressDropdown(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if(enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            enabled = enabled,
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        
        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1C140D))
    }
}
