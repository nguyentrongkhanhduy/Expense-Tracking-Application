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
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
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

    fun getTotalSpendingAndEarning(startDate: Long? = null, endDate: Long? = null): List<PieEntry> {
        val totalSpending = transactionsWithCategory.value
            .filter {
                it.transaction.type.lowercase() == "expense" &&
                        (startDate == null || it.transaction.date >= startDate) &&
                        (endDate == null || it.transaction.date <= endDate)
            }
            .sumOf { it.transaction.amount }

        val totalEarning = transactionsWithCategory.value
            .filter {
                it.transaction.type.lowercase() == "income" &&
                        (startDate == null || it.transaction.date >= startDate) &&
                        (endDate == null || it.transaction.date <= endDate)
            }
            .sumOf { it.transaction.amount }

        return listOfNotNull(
            if (totalSpending > 0) PieEntry(totalSpending.toFloat(), "Spending") else null,
            if (totalEarning > 0) PieEntry(totalEarning.toFloat(), "Earning") else null
        )
    }

    fun getSpendingByCategory(startDate: Long? = null, endDate: Long? = null): List<PieEntry> {
        val spendingByCategory = mutableMapOf<String, Float>()

        transactionsWithCategory.value.forEach { transactionWithCategory ->
            val categoryName = transactionWithCategory.category?.title ?: "Unknown"
            val transaction = transactionWithCategory.transaction

            if (
                transaction.type.lowercase() == "expense" &&
                (startDate == null || transaction.date >= startDate) &&
                (endDate == null || transaction.date <= endDate)
            ) {
                spendingByCategory[categoryName] =
                    (spendingByCategory[categoryName] ?: 0f) + transaction.amount.toFloat()
            }
        }

        return spendingByCategory.map { (categoryName, amount) ->
            PieEntry(amount, categoryName)
        }
            .sortedBy { it.label }
    }

    fun getEarningByCategory(startDate: Long? = null, endDate: Long? = null): List<PieEntry> {
        val earningByCategory = mutableMapOf<String, Float>()

        transactionsWithCategory.value.forEach { transactionWithCategory ->
            val categoryName = transactionWithCategory.category?.title ?: "Unknown"
            val transaction = transactionWithCategory.transaction

            if (
                transaction.type.lowercase() == "income" &&
                (startDate == null || transaction.date >= startDate) &&
                (endDate == null || transaction.date <= endDate)
            ) {
                earningByCategory[categoryName] =
                    (earningByCategory[categoryName] ?: 0f) + transaction.amount.toFloat()
            }
        }

        return earningByCategory.map { (categoryName, amount) ->
            PieEntry(amount, categoryName)
        }
            .sortedBy { it.label }
    }

    fun getBalance(startDate: Long? = null, endDate: Long? = null): Double {
        val totalSpending = transactionsWithCategory.value
            .filter {
                it.transaction.type.lowercase() == "expense" &&
                        (startDate == null || it.transaction.date >= startDate) &&
                        (endDate == null || it.transaction.date <= endDate)
            }
            .sumOf { it.transaction.amount }

        val totalEarning = transactionsWithCategory.value
            .filter {
                it.transaction.type.lowercase() == "income" &&
                        (startDate == null || it.transaction.date >= startDate) &&
                        (endDate == null || it.transaction.date <= endDate)
            }
            .sumOf { it.transaction.amount }

        return totalEarning - totalSpending
    }

    fun getTotalSpend(startDate: Long? = null, endDate: Long? = null): Double {
        return transactionsWithCategory.value
            .filter {
                it.transaction.type.lowercase() == "expense" &&
                        (startDate == null || it.transaction.date >= startDate) &&
                        (endDate == null || it.transaction.date <= endDate)
            }
            .sumOf { it.transaction.amount }
    }

    fun getTotalEarn(startDate: Long? = null, endDate: Long? = null): Double {
        return transactionsWithCategory.value
            .filter {
                it.transaction.type.lowercase() == "income" &&
                        (startDate == null || it.transaction.date >= startDate) &&
                        (endDate == null || it.transaction.date <= endDate)
            }
            .sumOf { it.transaction.amount }
    }

    fun getSpendingWithLimitByCategory(startDate: Long? = null, endDate: Long? = null): List<Pair<String, Double>> {
        return transactionsWithCategory.value
            .filter {
                it.transaction.type.lowercase() == "expense" &&
                        (startDate == null || it.transaction.date >= startDate) &&
                        (endDate == null || it.transaction.date <= endDate)
            }
            .groupBy { it.category?.title ?: "Unknown" }
            .map { (categoryName, transactions) ->
                val totalSpent = transactions.sumOf { it.transaction.amount }
                val limit = transactions.firstOrNull()?.category?.limit ?: 0.0
                "$categoryName|$limit" to totalSpent
            }
            .sortedBy { it.first }
    }


    fun getEarningWithTotalByCategory(startDate: Long? = null, endDate: Long? = null): List<Pair<String, Double>> {
        val totalEarned = transactionsWithCategory.value
            .filter {
                it.transaction.type.lowercase() == "income" &&
                        (startDate == null || it.transaction.date >= startDate) &&
                        (endDate == null || it.transaction.date <= endDate)
            }
            .sumOf { it.transaction.amount }

        return transactionsWithCategory.value
            .filter {
                it.transaction.type.lowercase() == "income" &&
                        (startDate == null || it.transaction.date >= startDate) &&
                        (endDate == null || it.transaction.date <= endDate)
            }
            .groupBy { it.category?.title ?: "Unknown" }
            .map { (categoryName, transactions) ->
                val categoryTotal = transactions.sumOf { it.transaction.amount }
                "$categoryName|$totalEarned" to categoryTotal
            }
            .sortedBy { it.first }
    }

    fun updateAllAmountsByExchangeRate(exchangeRate: Double) {
        viewModelScope.launch {
            transactionRepository.updateAllAmountsByExchangeRate(exchangeRate)
        }
    }

    fun getFinancialSummary(startDate: Long? = null, endDate: Long? = null): String {
        val totalSpend = getTotalSpend(startDate, endDate)
        val totalEarn = getTotalEarn(startDate, endDate)
        val balance = getBalance(startDate, endDate)
        val spendingByCat = getSpendingWithLimitByCategory(startDate, endDate)
            .joinToString("\n") { (cat, spent) ->
                val (name, limit) = cat.split("|")
                "$name: $spent spent (limit: $limit)"
            }
        return """
        Financial Overview:
        - Total spent: $totalSpend
        - Total earned: $totalEarn
        - Net balance: $balance
        - Category breakdown:
        $spendingByCat
    """.trimIndent()
    }

}
