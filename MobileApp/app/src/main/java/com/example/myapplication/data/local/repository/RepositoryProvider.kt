package com.example.myapplication.data.local.repository

import android.content.Context
import com.example.myapplication.data.local.DatabaseProvider

object RepositoryProvider {
    private var categoryRepository: CategoryRepository? = null

    fun getCategoryRepository(context: Context): CategoryRepository {
        return categoryRepository ?: synchronized(this) {
            val db = DatabaseProvider.getDatabase(context)
            val repo = CategoryRepository(db.categoryDao())
            categoryRepository = repo
            repo
        }
    }
}