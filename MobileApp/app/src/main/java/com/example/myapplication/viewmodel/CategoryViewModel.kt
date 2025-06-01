package com.example.myapplication.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    // Category list state
    private val _categories: StateFlow<List<Category>> = repository.getAllCategories()
        .map { it.sortedBy { cat -> cat.categoryId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories: StateFlow<List<Category>> = _categories

    // Dialog input state
    var inputType by mutableStateOf("")
    var inputTitle by mutableStateOf("")
    var inputIcon by mutableStateOf("")
    var inputLimit by mutableStateOf("")

    // Validation error messages
    var typeError by mutableStateOf<String?>(null)
    var titleError by mutableStateOf<String?>(null)
    var iconError by mutableStateOf<String?>(null)
    var limitError by mutableStateOf<String?>(null)

    private val validTypes = listOf("expense", "income")

    init {
        initializeDefaults()
    }

    private fun initializeDefaults() {
        viewModelScope.launch {
            repository.getAllCategories().collect { list ->
                if (list.isEmpty()) {
                    addCategory(Category(title = "Food", icon = "🍔", type = "expense"))
                    addCategory(Category(title = "Salary", icon = "💰", type = "income"))
                    addCategory(Category(title = "Transport", icon = "🚗", type = "expense"))
                    addCategory(Category(categoryId = -1L, title = "Others", icon = "📦", type = "expense"))
                    addCategory(Category(categoryId = -2L, title = "Others", icon = "📦", type = "income"))
                }
            }
        }
    }

    // --- Input Validation Logic ---

    fun validateInputs(): Boolean {
        typeError = if (inputType.isBlank() || inputType !in validTypes) "Select a valid type" else null
        titleError = when {
            inputTitle.isBlank() -> "Title cannot be empty"
            inputTitle.length > 30 -> "Title too long"
            else -> null
        }
        iconError = when {
            inputIcon.isBlank() -> "Icon required"
            inputIcon.codePointCount(0, inputIcon.length) != 1 -> "Use a single symbol or emoji"
            else -> null
        }

        limitError = when {
            inputLimit.isNotBlank() && inputLimit.toDoubleOrNull()?.let { it < 0 } == true -> "Limit must be positive"
            inputLimit.isNotBlank() && inputLimit.toDoubleOrNull() == null -> "Limit must be a number"
            else -> null
        }
        return listOf(typeError, titleError, iconError, limitError).all { it == null }
    }

    fun resetInputFields(
        type: String = "",
        title: String = "",
        icon: String = "",
        limit: String = ""
    ) {
        inputType = type
        inputTitle = title
        inputIcon = icon
        inputLimit = limit
        typeError = null
        titleError = null
        iconError = null
        limitError = null
    }

    // --- CRUD Operations ---

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
