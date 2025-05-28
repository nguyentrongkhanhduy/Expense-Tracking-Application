package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.model.Category
import com.example.myapplication.data.local.repository.CategoryRepository
import com.example.myapplication.data.local.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val repository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    private val _categories: StateFlow<List<Category>> = repository.getAllCategories()
        .map {
            it.sortedBy { cat-> cat.categoryId }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private fun initializeDefaults() {
        viewModelScope.launch {
            repository.getAllCategories().collect { list ->
                if (list.isEmpty()) {
                    addCategory(Category(title = "Food", icon = "🍔", type = "expense"))
                    addCategory(Category(title = "Salary", icon = "💰", type = "income"))
                    addCategory(Category(title = "Transport", icon = "🚗", type = "expense"))
                    addCategory(Category(categoryId = -1L, title = "Others", icon = "📦", type = "expense")) //default for expense
                    addCategory(Category(categoryId = -2L, title = "Others", icon = "📦", type = "income")) //default for income
                }
            }
        }
    }

    init {
        initializeDefaults()
    }

    fun addCategory(category: Category) {
        viewModelScope.launch {
            repository.insertCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        if (category.categoryId < 0L || category.title.equals("Others", ignoreCase = true)) {
            println("DEBUG: Cannot delete 'Others' category.")
            return
        }

        viewModelScope.launch {
            val fallbackId = if (category.type == "expense") -1L else -2L
            transactionRepository.reassignCategory(category.categoryId, fallbackId)
            repository.deleteCategory(category)
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }
}