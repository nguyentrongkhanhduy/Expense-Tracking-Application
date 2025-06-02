package com.example.myapplication.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.model.Transaction
import com.example.myapplication.data.local.model.TransactionWithCategory
import com.example.myapplication.data.local.repository.CategoryRepository
import com.example.myapplication.data.local.repository.TransactionRepository
import com.example.myapplication.helpers.sendBudgetExceededNotification
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    val transactions: StateFlow<List<Transaction>> =
        transactionRepository.getAllTransactions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactionsWithCategory: StateFlow<List<TransactionWithCategory>> =
        transactionRepository.getTransactionsWithCategory()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Input Fields for Dialogs ---
    var inputAmount by mutableStateOf("")
    var inputName by mutableStateOf("")
    var inputType by mutableStateOf("Expense")
    var inputCategoryId by mutableStateOf<Long?>(null)
    var inputNote by mutableStateOf("")
    var inputDate by mutableStateOf<Long?>(null)
    var inputLocation by mutableStateOf("")
    var inputImagePath by mutableStateOf<String?>(null)


    // --- Validation Error States ---
    var amountError by mutableStateOf<String?>(null)
    var nameError by mutableStateOf<String?>(null)
    var typeError by mutableStateOf<String?>(null)
    var categoryError by mutableStateOf<String?>(null)
    var locationError by mutableStateOf<String?>(null)

    private val validTypes = listOf("expense", "income")

    fun validateInputs(): Boolean {
        amountError =
            if (inputAmount.toDoubleOrNull() == null || inputAmount.toDouble() <= 0.0) "Amount must be greater than 0" else null
        nameError = if (inputName.isBlank()) "Name cannot be empty" else null
        typeError =
            if (inputType.isBlank() || inputType.lowercase() !in validTypes) "Select a valid type" else null
        categoryError = if (inputCategoryId == null) "Please select a category" else null
        locationError = if (inputLocation.isBlank()) "Location cannot be empty" else null
        return listOf(amountError, nameError, typeError, categoryError).all { it == null }
    }

    fun resetInputFields(
        amount: String = "",
        name: String = "",
        type: String = "Expense",
        categoryId: Long? = null,
        note: String = "",
        date: Long? = null,
        location: String = "",
        imagePath: String? = null
    ) {
        inputAmount = amount
        inputName = name
        inputType = type
        inputCategoryId = categoryId
        inputNote = note
        inputDate = date
        inputLocation = location
        inputImagePath = imagePath
        amountError = null
        nameError = null
        typeError = null
        categoryError = null
    }

    // --- CRUD Operations ---
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.insertTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
        }
    }

    fun checkAndNotifyBudget(context: Context, transaction: Transaction) {
        if (transaction.type.lowercase() != "expense") return
        val categoryId = transaction.categoryId
        if (categoryId < 0L) return

        viewModelScope.launch {
            val category = categoryRepository.getCategoryById(categoryId) ?: return@launch

            if (category.limit == null || category.limit == 0.0) return@launch

            // Calculate total including the new transaction
            val currentTotal = transactionsWithCategory.value
                .filter { it.transaction.categoryId == categoryId }
                .sumOf { it.transaction.amount }

            val totalIncludingNew = currentTotal + transaction.amount

            if (totalIncludingNew >= category.limit) {
                sendBudgetExceededNotification(
                    context,
                    totalIncludingNew,
                    category.limit,
                    category.title
                )
            }
        }
    }
}
