package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Transaction")
data class Transaction(
    @PrimaryKey(autoGenerate = false) val transactionId: Long = 0L,
    val amount: Double,
    val name: String,
    val type: String, // "income" or "expense"
    val categoryId: Long, //ref to Category table
    val note: String? = null,
    val date: Long, // Store as timestamp in milliseconds
    val location: String? = null,
    val imageUrl: String? = null,
)
