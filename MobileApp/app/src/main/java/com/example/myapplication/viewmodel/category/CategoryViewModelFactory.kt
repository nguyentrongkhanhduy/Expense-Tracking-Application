package com.example.myapplication.viewmodel.category

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.local.DatabaseProvider
import com.example.myapplication.data.local.repository.CategoryRepository
import com.example.myapplication.data.local.repository.RepositoryProvider
import com.example.myapplication.data.local.repository.TransactionRepository

class CategoryViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            val categoryRepo = RepositoryProvider.getCategoryRepository(context)
            val transactionRepo = RepositoryProvider.getTransactionRepository(context)
            return CategoryViewModel(categoryRepo, transactionRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
