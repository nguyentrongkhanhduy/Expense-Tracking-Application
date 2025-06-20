package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM Category")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM Category WHERE categoryId = :categoryId")
    suspend fun getCategoryById(categoryId: Long): Category?

    @Query("UPDATE Category SET isDeleted = 1 WHERE categoryId = :categoryId")
    suspend fun softDeleteCategory(categoryId: Long)

    @Query("DELETE FROM Category WHERE categoryId > 0")
    suspend fun clearCategories()
}