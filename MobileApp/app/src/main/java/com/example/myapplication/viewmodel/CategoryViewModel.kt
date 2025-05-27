package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.model.Category
import com.example.myapplication.data.local.repository.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val repository: CategoryRepository
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
                    addCategory(Category(title = "Others", icon = "📦", type = "expense"))
                }
            }
        }
    }

    init {
        initializeDefaults()
    }

    fun printCategories() {
        viewModelScope.launch {
            categories.collect { list ->
                println("DEBUG: Current categories:")
                list.forEach { println(it) }
            }
        }
    }

    fun addCategory(category: Category) {
        viewModelScope.launch {
            repository.insertCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }
}