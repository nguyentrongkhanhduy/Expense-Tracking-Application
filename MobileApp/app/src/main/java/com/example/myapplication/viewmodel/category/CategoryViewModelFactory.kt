package com.example.myapplication.viewmodel.category

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.local.DatabaseProvider
import com.example.myapplication.data.local.repository.CategoryRepository
import com.example.myapplication.data.local.repository.TransactionRepository

class CategoryViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            val db = DatabaseProvider.getDatabase(context)
            val repo = CategoryRepository(db.categoryDao())
            val transactionRepo = TransactionRepository(db.transactionDao())
            return CategoryViewModel(repo, transactionRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
