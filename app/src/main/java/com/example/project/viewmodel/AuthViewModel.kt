package com.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.data.entities.User
import com.example.project.data.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: UserRepository) : ViewModel() {

    // Hàm xử lý Đăng nhập: Trả về đối tượng User nếu thành công
    fun login(user: String, pass: String, onResult: (User?) -> Unit) {
        viewModelScope.launch {
            val account = repository.login(user, pass)
            onResult(account)
        }
    }

    // Hàm xử lý Đăng ký
    fun register(user: User, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val existing = repository.checkUserExists(user.username)
            if (existing != null) {
                onResult(false, "Tên tài khoản đã tồn tại!")
            } else {
                repository.register(user)
                onResult(true, "Đăng ký thành công!")
            }
        }
    }
}