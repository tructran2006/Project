package com.example.project.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.project.data.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // Đăng ký tài khoản mới
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun register(user: User)

    // Kiểm tra đăng nhập
    @Query("SELECT * FROM user_table WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    // Kiểm tra sự tồn tại
    @Query("SELECT * FROM user_table WHERE username = :user LIMIT 1")
    suspend fun checkUserExists(user: String): User?

    @Query("SELECT * FROM user_table WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    // --- QUẢN LÝ ROLE (DÀNH CHO ADMIN VÀ SHOP) ---

    // Cập nhật Role (Dùng khi Admin duyệt một User lên làm Shop)
    @Query("UPDATE user_table SET role = :newRole WHERE email = :email")
    suspend fun updateUserRole(email: String, newRole: String)

    // Lấy danh sách tất cả User (Dành cho Admin quản lý)
    @Query("SELECT * FROM user_table")
    fun getAllUsersFlow(): Flow<List<User>>

    // Lấy danh sách tất cả các Shop (Để hiện danh sách đối tác)
    @Query("SELECT * FROM user_table WHERE role = 'shop'")
    fun getAllShopsFlow(): Flow<List<User>>

    // Hàm cụ thể để người dùng tự nâng cấp lên Shop
    @Query("UPDATE user_table SET role = 'shop' WHERE email = :email")
    suspend fun upgradeToShop(email: String)
    // --- CẬP NHẬT THÔNG TIN CÁ NHÂN ---
    @Query("""
        UPDATE user_table 
        SET username = :name, 
            birthday = :birthday, 
            age = :age, 
            address = :address,
            phone = :phone
        WHERE email = :email
    """)
    suspend fun updateUserInfo(
        email: String,
        name: String,
        birthday: String,
        age: Int,
        address: String,
        phone: String
    )

    // Cập nhật ảnh đại diện
    @Query("UPDATE user_table SET avatarUri = :uri WHERE email = :email")
    suspend fun updateAvatar(email: String, uri: String)
}