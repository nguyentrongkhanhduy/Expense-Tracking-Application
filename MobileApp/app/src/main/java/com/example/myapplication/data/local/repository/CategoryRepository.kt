package com.example.myapplication.data.local.repository

import com.example.myapplication.data.local.dao.CategoryDao
import com.example.myapplication.data.model.Category
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val dao: CategoryDao) {
    fun getAllCategories(): Flow<List<Category>> = dao.getAllCategories()

    suspend fun getCategoryById(id: Long): Category? = dao.getCategoryById(id)

    suspend fun insertCategory(category: Category): Long = dao.insertCategory(category)

    suspend fun updateCategory(category: Category) = dao.updateCategory(category)

    suspend fun deleteCategory(category: Category) = dao.deleteCategory(category)

    suspend fun softDeleteCategory(categoryId: Long) = dao.softDeleteCategory(categoryId)

    suspend fun clearCategories() = dao.clearCategories()
}