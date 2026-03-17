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
    @Query("SELECT * FROM user_table WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    // Kiểm tra xem tên tài khoản đã tồn tại chưa
    @Query("SELECT * FROM user_table WHERE username = :user LIMIT 1")
    suspend fun checkUserExists(user: String): User?

    @Query("SELECT * FROM user_table WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("UPDATE user_table SET role = :newRole WHERE email = :email")
    suspend fun updateUserRole(email: String, newRole: String)

    @Query("UPDATE user_table SET username = :name, birthday = :birthday, age = :age, address = :address WHERE email = :email")
    suspend fun updateUserInfo(email: String, name: String, birthday: String, age: Int, address: String)
}