package com.example.project.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.project.data.dao.CategoryDao
import com.example.project.data.dao.OrderDao
import com.example.project.data.dao.ProductDao
import com.example.project.data.dao.UserDao
import com.example.project.data.entities.CartItem
import com.example.project.data.entities.Category
import com.example.project.data.entities.Order
import com.example.project.data.entities.Product
import com.example.project.data.entities.User

@Database(entities = [User::class, Product::class, Category::class, Order::class, CartItem::class],
    version = 7,
    exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shop_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}