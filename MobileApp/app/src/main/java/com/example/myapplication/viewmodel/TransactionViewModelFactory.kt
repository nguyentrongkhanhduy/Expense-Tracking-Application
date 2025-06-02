package com.example.myapplication.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.local.DatabaseProvider
import com.example.myapplication.data.local.repository.CategoryRepository
import com.example.myapplication.data.local.repository.TransactionRepository

class TransactionViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            val db = DatabaseProvider.getDatabase(context)
            val repo = TransactionRepository(db.transactionDao())
            val categoryRepo = CategoryRepository(db.categoryDao())
            return TransactionViewModel(repo, categoryRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}