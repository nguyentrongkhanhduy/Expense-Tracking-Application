package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Category")
data class Category(
    @PrimaryKey(autoGenerate = false) val categoryId: Long = 0,
    val type: String,
    val title: String,
    val icon: String,
    val limit: Double? = null,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)
