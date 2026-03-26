package com.example.project.utils

import android.content.Context

class SharedPrefs {

    companion object {
        private const val PREF_NAME = "app_prefs"
        private const val FAVORITE_KEY = "favorite_products"

        // Lấy danh sách id sản phẩm yêu thích
        fun getFavoriteIds(context: Context): MutableSet<String> {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return prefs.getStringSet(FAVORITE_KEY, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        }

        // Kiểm tra sản phẩm đã được yêu thích chưa
        fun isFavorite(context: Context, productId: Int): Boolean {
            return getFavoriteIds(context).contains(productId.toString())
        }

        // Thêm sản phẩm vào yêu thích
        fun addFavorite(context: Context, productId: Int) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val favorites = getFavoriteIds(context)
            favorites.add(productId.toString())
            prefs.edit().putStringSet(FAVORITE_KEY, favorites).apply()
        }

        // Xóa sản phẩm khỏi yêu thích
        fun removeFavorite(context: Context, productId: Int) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val favorites = getFavoriteIds(context)
            favorites.remove(productId.toString())
            prefs.edit().putStringSet(FAVORITE_KEY, favorites).apply()
        }

        // Đổi trạng thái yêu thích
        fun toggleFavorite(context: Context, productId: Int) {
            if (isFavorite(context, productId)) {
                removeFavorite(context, productId)
            } else {
                addFavorite(context, productId)
            }
        }
    }
}