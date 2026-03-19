package com.example.project.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.project.data.entities.Category

@Dao
interface CategoryDao {
    @Insert
    suspend fun insertCategory(category: Category)

    @Query("SELECT * FROM category_table")
    fun getAllCategories(): kotlinx.coroutines.flow.Flow<List<Category>>
}