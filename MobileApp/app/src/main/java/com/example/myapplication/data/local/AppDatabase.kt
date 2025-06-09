package com.example.myapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.data.local.dao.CategoryDao
import com.example.myapplication.data.local.dao.TransactionDao
import com.example.myapplication.data.model.Category
import com.example.myapplication.data.model.Transaction

@Database(
    entities = [Category::class, Transaction::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
}