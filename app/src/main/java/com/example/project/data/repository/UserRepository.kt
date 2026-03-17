package com.example.project.data.repository

import com.example.project.data.dao.UserDao
import com.example.project.data.entities.User

class UserRepository(private val userDao: UserDao) {

    // Gọi lệnh đăng ký từ Dao
    suspend fun register(user: User) {
        userDao.register(user)
    }

    // Gọi lệnh đăng nhập từ Dao, trả về User nếu đúng, null nếu sai
    suspend fun login(username: String, password: String): User? {
        return userDao.login(username, password)
    }

    // Kiểm tra tên tài khoản đã tồn tại chưa
    suspend fun checkUserExists(username: String): User? {
        return userDao.checkUserExists(username)
    }
}