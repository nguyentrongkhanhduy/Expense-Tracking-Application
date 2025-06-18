package com.example.myapplication.viewmodel.transaction

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Transaction
import com.example.myapplication.data.model.TransactionWithCategory
import com.example.myapplication.data.local.repository.CategoryRepository
import com.example.myapplication.data.local.repository.TransactionRepository
import com.example.myapplication.data.model.Category
import com.example.myapplication.helpers.sendBudgetExceededNotification
import com.example.myapplication.services.CategoryApiService
import com.example.myapplication.services.ImageRequest
import com.example.myapplication.services.ReassignCategoryRequest
import com.example.myapplication.services.RequestedImage
import com.example.myapplication.services.RetrofitClient
import com.example.myapplication.services.TransactionApiService
import com.example.myapplication.services.TransactionRequest
import com.example.myapplication.services.UserIdRequest
import com.github.mikephil.charting.data.PieEntry
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

    // --- CRUD Operations Room database---
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

    fun softDeleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.softDeleteTransaction(transaction.transactionId)
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

        transactionsWithCategory.value
            .forEach { transactionWithCategory ->
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

        transactionsWithCategory.value
            .forEach { transactionWithCategory ->
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

    fun getSpendingWithLimitByCategory(
        startDate: Long? = null,
        endDate: Long? = null
    ): List<Pair<String, Double>> {
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

    fun getEarningWithTotalByCategory(
        startDate: Long? = null,
        endDate: Long? = null
    ): List<Pair<String, Double>> {
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

    private val transactionApiService =
            /*---- For Android Studio  ----*/
        //RetrofitClient.createService(TransactionApiService::class.java, "http://10.0.2.2:3000") //Simulator

        /*---- For Physical Device  ----*/
        RetrofitClient.createService(CategoryApiService::class.java, "https://expense-app-server-mocha.vercel.app")
    //CRUD firestore/firebase storage
    fun addTransactionToFirestore(userId: String, transaction: Transaction) {
        viewModelScope.launch {
            try {
                val response =
                    transactionApiService.createTransaction(TransactionRequest(userId, transaction))
                println("Added transaction to Firestore: $response")
            } catch (e: Exception) {
                println("Error: ${e.message}")
            }
        }
    }

    fun updateTransactionInFirestore(userId: String, transaction: Transaction) {
        viewModelScope.launch {
            try {
                val response = transactionApiService.updateTransaction(
                    transaction.transactionId,
                    TransactionRequest(userId, transaction)
                )
                println("Updated transaction in Firestore: $response")
            } catch (e: Exception) {
                println("Error: ${e.message}")
            }
        }
    }

    fun reassignCategoryInFirestore(userId: String, category: Category) {
        viewModelScope.launch {
            try {
                val oldCategoryId = category.categoryId
                val newCategoryId = if (category.type == "expense") -1L else -2L

                val response =
                    transactionApiService.reassignCategory(
                        ReassignCategoryRequest(
                            userId,
                            oldCategoryId,
                            newCategoryId
                        )
                    )
                println("Reassigned category in Firestore: $response")
            } catch (e: Exception) {
                println("Error: ${e.message}")
            }
        }
    }

    fun deleteTransactionFromFirestore(userId: String, transactionId: Long) {
        viewModelScope.launch {
            try {
                val response =
                    transactionApiService.deleteTransaction(transactionId, UserIdRequest(userId))
                println("Deleted transaction from Firestore: $response")
            } catch (e: Exception) {
                println("Error: ${e.message}")
            }
        }
    }

    suspend fun getTransactionsFromFirestore(userId: String): List<Transaction> {
        try {
            val response = transactionApiService.getTransactions(UserIdRequest(userId))
            println("Fetched transactions from Firestore: $response")
            return response
        } catch (e: Exception) {
            println("Error: ${e.message}")
            return emptyList()
        }
    }

    fun syncDataWhenSignUp(userId: String) {
        viewModelScope.launch {
            if (transactions.value.isNotEmpty()) {
                transactions.value
                    .filter { !it.isDeleted }
                    .forEach { addTransactionToFirestore(userId, it) }

                //hard delete after sync
                transactions.value
                    .filter { it.isDeleted }
                    .forEach { deleteTransaction(it) }
            }
        }
    }

    fun syncDataWhenLogIn(userId: String) {
        viewModelScope.launch {
            val remoteTransactions = getTransactionsFromFirestore(userId)
            val localTransactions =
                transactions.value //get all transactions both deleted and not deleted

            if (remoteTransactions.isNotEmpty()) {
                if (transactions.value.isEmpty()) {
                    remoteTransactions.forEach { addTransaction(it) }
                } else {
                    //TO DO: HANDLE CONFLICTS
                    //sync local first
                    val localMap = localTransactions.associateBy { it.transactionId }
                    remoteTransactions.forEach { remote ->
                        val local = localMap[remote.transactionId]
                        when {
                            local == null -> addTransaction(remote)

                            local.isDeleted -> {
                                deleteTransaction(local)
                                deleteTransactionFromFirestore(userId, remote.transactionId)
                            }

                            remote.updatedAt > local.updatedAt -> {
                                updateTransaction(remote)
                            }

                            remote.updatedAt < local.updatedAt -> {
                                updateTransactionInFirestore(userId, local)
                            }
                        }
                    }

                    //sync remote next
                    val remoteMap = remoteTransactions.associateBy { it.transactionId }
                    localTransactions.forEach { local ->
                        val remote = remoteMap[local.transactionId]
                        if (remote == null) {
                            if (local.isDeleted) {
                                deleteTransaction(local)
                            } else {
                                addTransactionToFirestore(userId, local)
                            }
                        }
                    }
                }
            }
        }
    }

    fun uploadImageToFirebaseStorage(
        userId: String,
        requestedImage: RequestedImage,
        onImageUploaded: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response =
                    transactionApiService.uploadImage(ImageRequest(userId, requestedImage))
                if (response.success) {
                    onImageUploaded(response.imageUrl!!)
                } else {
                    println("Error uploading image: ${response.error}")
                }
            } catch (e: Exception) {
                println("Error: ${e.message}")
            }
        }
    }
}
