package com.example.myapplication.viewmodel.category

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Category
import com.example.myapplication.data.local.repository.CategoryRepository
import com.example.myapplication.data.local.repository.TransactionRepository
import com.example.myapplication.services.CategoryApiService
import com.example.myapplication.services.CategoryRequest
import com.example.myapplication.services.InitialCategoriesRequest
import com.example.myapplication.services.RetrofitClient
import com.example.myapplication.services.UserIdRequest
import com.github.mikephil.charting.utils.Utils.init
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    // Category list state
    private val _categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .map { it.sortedBy { cat -> cat.categoryId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories: StateFlow<List<Category>> =
        _categories //Both deleted and not deleted -> filter to use
    private var defaultCategories = _categories.value

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

    fun initializeDefaults(userId: String = "") {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { list ->
                if (list.isEmpty()) {
                    defaultCategories = listOf(
                        Category(
                            categoryId = System.currentTimeMillis() + 1,
                            title = "Food",
                            icon = "🍔",
                            type = "expense",
                            updatedAt = System.currentTimeMillis()
                        ),
                        Category(
                            categoryId = System.currentTimeMillis() + 2,
                            title = "Salary",
                            icon = "💰",
                            type = "income",
                            updatedAt = System.currentTimeMillis()

                        ),
                        Category(
                            categoryId = System.currentTimeMillis() + 3,
                            title = "Transport",
                            icon = "🚗",
                            type = "expense",
                            updatedAt = System.currentTimeMillis()
                        ),
                        Category(
                            categoryId = -1L,
                            title = "Others",
                            icon = "📦",
                            type = "expense",
                            updatedAt = -1L
                        ),
                        Category(
                            categoryId = -2L,
                            title = "Others",
                            icon = "📦",
                            type = "income",
                            updatedAt = -2L
                        )
                    )

                    defaultCategories.forEach { addCategory(it) }

                    if (userId.isNotEmpty()) {
                        syncDataWhenSignUp(userId)
                    }
                }
            }
        }
    }

    // --- Input Validation Logic ---

    fun validateInputs(): Boolean {
        typeError =
            if (inputType.isBlank() || inputType !in validTypes) "Select a valid type" else null
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
            inputLimit.isNotBlank() && inputLimit.toDoubleOrNull()
                ?.let { it < 0 } == true -> "Limit must be positive"

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

    // --- CRUD Operations for Room ---

    fun addCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.insertCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        if (category.categoryId < 0L || category.title.equals("Others", ignoreCase = true)) {
            println("DEBUG: Cannot delete 'Others' category.")
            return
        }
        viewModelScope.launch {
//            val fallbackId = if (category.type == "expense") -1L else -2L
//            transactionRepository.reassignCategory(category.categoryId, fallbackId)
            categoryRepository.deleteCategory(category)
        }
    }

    fun softDeleteCategory(category: Category) {
        viewModelScope.launch {
            val fallbackId = if (category.type == "expense") -1L else -2L
            transactionRepository.reassignCategory(category.categoryId, fallbackId)
            categoryRepository.softDeleteCategory(category.categoryId)
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.updateCategory(category)
        }
    }
    /*---- For Physical Device  ----*/
    private val categoryApiService = RetrofitClient.createService(CategoryApiService::class.java, "https://expense-app-server-mocha.vercel.app/")

    /*---- For Android Studio  ----*/
    //RetrofitClient.createService(CategoryApiService::class.java, "http://10.0.2.2:3000")

    // --- CRUD Operations for Firestore ---
    fun addCategoryToFirestore(category: Category, userId: String) {
        viewModelScope.launch {
            try {
                val response = categoryApiService.createCategory(CategoryRequest(userId, category))
                println("Added category to Firestore: $response")
            } catch (e: Exception) {
                println("Error: ${e.message}")
            }
        }
    }

    fun updateCategoryInFirestore(category: Category, userId: String) {
        viewModelScope.launch {
            try {
                val response = categoryApiService.updateCategory(
                    category.categoryId,
                    CategoryRequest(userId, category)
                )
                println("Updated category in Firestore: $response")
            } catch (e: Exception) {
                println("Error: ${e.message}")
            }
        }
    }

    fun deleteCategoryFromFirestore(categoryId: Long, userId: String) {
        viewModelScope.launch {
            try {
                val response = categoryApiService.deleteCategory(categoryId, UserIdRequest(userId))
                println("Deleted category from Firestore: $response")
            } catch (e: Exception) {
                println("Error: ${e.message}")
            }
        }
    }

    suspend fun getCategoriesFromFirestore(userId: String): List<Category> {
        try {
            val response = categoryApiService.getCategories(UserIdRequest(userId))
            println("Fetched categories from Firestore: $response")
            return response
        } catch (e: Exception) {
            println("Error: ${e.message}")
            return emptyList()
        }
    }


    fun syncDataWhenSignUp(userId: String) {
        viewModelScope.launch {
            try {
                if (categories.value.isNotEmpty()) {
                    defaultCategories = categories.value.filter { !it.isDeleted }
                }
                val response = categoryApiService.createInitialCategories(
                    InitialCategoriesRequest(
                        userId,
                        defaultCategories
                    )
                )

                //hard delete after sync
                categories.value.filter { it.isDeleted }.forEach {
                    deleteCategory(it)
                }

                println("Synced default categories to Firestore: $response")
            } catch (e: Exception) {
                println("Error: ${e.message}")
            }
        }
    }

    fun syncDataWhenLogIn(userId: String) {
        viewModelScope.launch {
            val remoteCategories = getCategoriesFromFirestore(userId)
            val localCategories = categories.value //get all categories both deleted and not deleted

            if (remoteCategories.isNotEmpty()) {
                if (localCategories.isEmpty()) {
                    remoteCategories.forEach { addCategory(it) }
                } else {
                    //TO DO: HANDLE CONFLICTS
                    //sync local first
                    val localMap = localCategories.associateBy { it.categoryId }
                    remoteCategories.forEach { remote ->
                        if (remote.categoryId < 0L) {
                            return@forEach
                        }

                        val local = localMap[remote.categoryId]
                        when {
                            local == null -> addCategory(remote)
                            local.isDeleted -> {
                                deleteCategory(local)
                                deleteCategoryFromFirestore(remote.categoryId, userId)
                            }

                            remote.updatedAt > local.updatedAt -> {
                                updateCategory(remote)
                            }

                            remote.updatedAt < local.updatedAt -> {
                                updateCategoryInFirestore(local, userId)
                            }
                        }
                    }

                    //sync remote next
                    val remoteMap = remoteCategories.associateBy { it.categoryId }
                    localCategories.forEach { local ->
                        if (local.categoryId < 0L) return@forEach

                        val remote = remoteMap[local.categoryId]
                        if (remote == null) {
                            if (local.isDeleted) {
                                deleteCategory(local)
                            } else {
                                addCategoryToFirestore(local, userId)
                            }
                        }
                    }

                }
            }
        }
    }
}
