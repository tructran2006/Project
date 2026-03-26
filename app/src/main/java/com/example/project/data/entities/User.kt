package com.example.project.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val password: String,
    val email: String,
    val role: String = "user",
    val birthday: String? = "Chưa cập nhật",
    val age: Int? = 0,
    val address: String? = "Chưa cập nhật",
    val phone: String? = "",
    val avatarUri: String? = null
)