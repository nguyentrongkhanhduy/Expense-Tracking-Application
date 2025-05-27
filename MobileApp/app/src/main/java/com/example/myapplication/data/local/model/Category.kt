package com.example.myapplication.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Category")
data class Category(
    @PrimaryKey(autoGenerate = true) val categoryId: Long = 0,
    val type: String,
    val title: String,
    val icon: String,
    val limit: Double? = null
)
