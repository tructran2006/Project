package com.example.project.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.project.data.entities.User

@Dao
interface UserDao {
    // Đăng ký tài khoản mới
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun register(user: User)

    // Kiểm tra đăng nhập
    @Query("SELECT * FROM user_table WHERE username = :user AND password = :pass LIMIT 1")
    suspend fun login(user: String, pass: String): User?

    // Kiểm tra xem tên tài khoản đã tồn tại chưa
    @Query("SELECT * FROM user_table WHERE username = :user LIMIT 1")
    suspend fun checkUserExists(user: String): User?
}