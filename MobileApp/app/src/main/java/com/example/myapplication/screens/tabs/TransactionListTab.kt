package com.example.myapplication.screens.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.components.MyButton
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.ui.theme.PrimaryRed
import com.example.myapplication.ui.theme.White
import com.example.myapplication.R
import com.example.myapplication.data.model.TransactionWithCategory
import com.example.myapplication.screens.dialogs.EditTransactionDialog
import com.example.myapplication.viewmodel.category.CategoryViewModel
import com.example.myapplication.viewmodel.transaction.TransactionViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.components.AdBanner
import com.example.myapplication.components.CustomSegmentedTabRow
import com.example.myapplication.helpers.getRequestedImage
import com.example.myapplication.helpers.removeFromInternalStorage
import com.example.myapplication.screens.dialogs.AddTransactionDialog
import com.example.myapplication.screens.dialogs.ConfirmationDialog
import com.example.myapplication.screens.dialogs.CustomCategoryFilterDialog
import com.example.myapplication.screens.dialogs.CustomDateRangeDialog
import com.example.myapplication.screens.dialogs.StyledAlertDialog
import com.example.myapplication.ui.theme.PrimaryGreen
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.LocationViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Composable
fun TransactionListTab(
    transactionViewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel,
    locationViewModel: LocationViewModel,
    authViewModel: AuthViewModel,
    currencySymbol: String,

    ) {
    val user by authViewModel.user.collectAsState()
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val tabTexts = listOf("All", "Today", "Week", "Month", "Custom")
    var selectedTab by remember { mutableIntStateOf(0) }
    val transactionsWithCategory by transactionViewModel.transactionsWithCategory.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()
    var editingTransaction by remember { mutableStateOf<TransactionWithCategory?>(null) }

    val sortOptions = listOf("New - Old", "Old - New", "High - Low", "Low - High")
    var selectedSortOption by remember { mutableStateOf(sortOptions[0]) }
    var sortDropdownExpanded by remember { mutableStateOf(false) }

    var customDateDialogExpanded by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }

    var customCategoryFilterDialogExpanded by remember { mutableStateOf(false) }
    var selectedCategoryTypeToFilter by remember { mutableStateOf<String>("") }
    var selectedCategoryToFilter by remember { mutableStateOf<Long?>(null) }

    var showAddDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    var showExportDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    var completeTitle by remember { mutableStateOf("") }
    var completeMessage by remember { mutableStateOf("") }

    LaunchedEffect(selectedTab) {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        when (selectedTab) {
            0 -> {
                startDate = null
                endDate = null
            }

            1 -> {
                // Today
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startDate = calendar.timeInMillis
                endDate = now
            }

            2 -> {
                // Week
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startDate = calendar.timeInMillis
                endDate = now
            }

            3 -> {
                // Month
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startDate = calendar.timeInMillis
                endDate = now
            }

            4 -> {
                customDateDialogExpanded = true
            }
        }
        println("Selected tab: $selectedTab, Start date: $startDate, End date: $endDate")
    }

    // Custom date range dialog
    if (customDateDialogExpanded) {
        CustomDateRangeDialog(
            onDismiss = { customDateDialogExpanded = false },
            onConfirm = { start, end ->
                startDate = start
                endDate = end
                customDateDialogExpanded = false
            }
        )
    }

    if (customCategoryFilterDialogExpanded) {
        CustomCategoryFilterDialog(
            onDismiss = { customCategoryFilterDialogExpanded = false },
            onConfirm = { type, categoryId ->
                selectedCategoryTypeToFilter = type
                selectedCategoryToFilter = categoryId
                customCategoryFilterDialogExpanded = false
            },
            categoryList = categories
        )
    }

    if (showExportDialog) {
        ConfirmationDialog(
            title = "Export Transactions",
            message = "Are you sure you want to export all transactions?",
            onConfirm = {
                showExportDialog = false
                transactionViewModel.exportTransactionsToCSV { title, message ->
                    completeTitle = title
                    completeMessage = message
                    showCompleteDialog = true
                }
            },
            onCancel = { showExportDialog = false },
            onDismiss = { showExportDialog = false }
        )
    }

    if (showCompleteDialog) {
        StyledAlertDialog(
            title = completeTitle,
            message = completeMessage,
            onDismiss = { showCompleteDialog = false },
            onConfirm = { showCompleteDialog = false }
        )
    }

    val filteredTransactions = transactionsWithCategory
        .filter {
            val query = searchQuery.text.trim()
            if (query.isBlank()) true
            else if (query.toDoubleOrNull() != null) {
                it.transaction.amount.toString().contains(query)
            } else {
                it.transaction.name.contains(query, ignoreCase = true)
            }
        }
        .let { list ->
            when (selectedSortOption) {
                "New - Old" -> list.sortedByDescending { it.transaction.date }
                "Old - New" -> list.sortedBy { it.transaction.date }
                "High - Low" -> list.sortedByDescending { it.transaction.amount }
                "Low - High" -> list.sortedBy { it.transaction.amount }
                else -> list
            }
        }
        .filter {
            val start = startDate ?: Long.MIN_VALUE
            val end = endDate ?: Long.MAX_VALUE
            it.transaction.date in start..end
        }
        .filter {
            if (selectedCategoryToFilter == null && selectedCategoryTypeToFilter.isNotBlank()) {
                it.category?.type.equals(selectedCategoryTypeToFilter, ignoreCase = true)
            } else if (selectedCategoryToFilter != null && selectedCategoryTypeToFilter.isNotBlank()) {
                it.category?.type.equals(
                    selectedCategoryTypeToFilter,
                    ignoreCase = true
                ) && it.category?.categoryId == selectedCategoryToFilter
            } else {
                true
            }
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 18.dp)
        ) {
            AdBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )
            Spacer(Modifier.height(10.dp))

            // Title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Text(
                    text = "Transactions",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    modifier = Modifier.align(Alignment.Center)
                )

                IconButton(
                    onClick = {
                        showExportDialog = true
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.download),
                        contentDescription = "Settings",
                        tint = PrimaryBlue
                    )
                }
            }

            // Search Bar as OutlinedTextField
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = PrimaryBlue
                    )
                },
                placeholder = {
                    Text(
                        text = "Search",
                        color = Color.Gray
                    )
                },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = PrimaryBlue,
                    cursorColor = PrimaryBlue
                )
            )
            Spacer(modifier = Modifier.height(18.dp))

            // Custom Segmented Tab Row (NO CHECKMARK)
            CustomSegmentedTabRow(
                tabTexts = tabTexts,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Sort and Filter Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MyButton(
                        onClick = { sortDropdownExpanded = true },
                        backgroundColor = PrimaryBlue
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.sort),
                            contentDescription = "Sort",
                            tint = White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedSortOption,
                            color = White,
                            fontWeight = FontWeight.Medium,
                        )
                        DropdownMenu(
                            expanded = sortDropdownExpanded,
                            onDismissRequest = { sortDropdownExpanded = false }
                        ) {
                            sortOptions.forEach {
                                DropdownMenuItem(
                                    text = { Text(it) },
                                    onClick = {
                                        selectedSortOption = it
                                        sortDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    MyButton(
                        onClick = { customCategoryFilterDialogExpanded = true },
                        backgroundColor = PrimaryBlue
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.filter),
                            contentDescription = "Filter",
                            tint = White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Filter",
                            color = White,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp), // Add enough bottom padding
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredTransactions) { transactionWithCategory ->
                    TransactionCard(
                        transactionWithCategory = transactionWithCategory,
                        currencySymbol = currencySymbol,
                        onClick = { editingTransaction = transactionWithCategory }
                    )
                }
            }

        }
    }
    // --- AddTransactionDialog ---
    if (showAddDialog) {
        AddTransactionDialog(
            viewModel = transactionViewModel,
            onDismiss = { showAddDialog = false },
            onSave = { transaction, bitmap, uri ->
                showAddDialog = false

                transactionViewModel.addTransaction(transaction)
                transactionViewModel.checkAndNotifyBudget(context, transaction)

                if (user != null) {
                    val requestedImage = getRequestedImage(
                        context, uri, bitmap,
                        transaction.transactionId.toString()
                    )
                    if (requestedImage != null) {
                        transactionViewModel.uploadImageToFirebaseStorage(
                            user!!.uid,
                            requestedImage
                        ) { url ->
                            val copyTransaction = transaction.copy(imageUrl = url)
                            transactionViewModel.addTransactionToFirestore(
                                user!!.uid,
                                copyTransaction
                            )
                        }
                    } else {
                        transactionViewModel.addTransactionToFirestore(user!!.uid, transaction)
                    }
                }

            },
            categoryList = categories,
            locationViewModel = locationViewModel,
            authViewModel = authViewModel
        )
    }
    // Edit dialog
    if (editingTransaction != null) {
        EditTransactionDialog(
            transaction = editingTransaction!!.transaction,
            onDismiss = { editingTransaction = null },
            onSave = { updatedTransaction, bitmap, uri ->
                transactionViewModel.updateTransaction(updatedTransaction)
                editingTransaction = null
                if (user != null) {
                    val requestedImage = getRequestedImage(
                        context, uri, bitmap,
                        updatedTransaction.transactionId.toString()
                    )

                    if (requestedImage != null) {
                        transactionViewModel.updateImageInFirebaseStorage(
                            user!!.uid,
                            requestedImage
                        ) { url ->
                            val copyTransaction = updatedTransaction.copy(imageUrl = url)
                            transactionViewModel.updateTransactionInFirestore(
                                user!!.uid,
                                copyTransaction
                            )
                        }
                    } else {
                        transactionViewModel.updateTransactionInFirestore(
                            user!!.uid,
                            updatedTransaction
                        )
                    }
                }
                transactionViewModel.checkAndNotifyBudget(context, updatedTransaction)
            },
            onDelete = {
                val imagePath = editingTransaction!!.transaction.imageUrl
                if (imagePath?.startsWith("bitmap:") == true) {
                    removeFromInternalStorage(imagePath)
                }
                transactionViewModel.softDeleteTransaction(editingTransaction!!.transaction)

                if (user != null) {
                    transactionViewModel.deleteImageFromFirebaseStorage(
                        user!!.uid,
                        editingTransaction!!.transaction.transactionId.toString()
                    )
                    transactionViewModel.deleteTransactionFromFirestore(
                        user!!.uid,
                        editingTransaction!!.transaction.transactionId
                    )
                    transactionViewModel.deleteTransaction(editingTransaction!!.transaction)
                }

                editingTransaction = null
            },
            categoryList = categories,
            locationViewModel = locationViewModel,
            viewModel = transactionViewModel,
            authViewModel = authViewModel
        )
    }
}


@Composable
fun TransactionCard(
    transactionWithCategory: TransactionWithCategory,
    currencySymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transaction = transactionWithCategory.transaction
    val category = transactionWithCategory.category
    val isIncome = transaction.type == "income"
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isIncome) PrimaryGreen else PrimaryRed,
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                val utcFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val formattedDate = utcFormat.format(java.util.Date(transaction.date))
                Text(formattedDate, color = White, fontWeight = FontWeight.SemiBold)
                Text(transaction.name, color = White)
            }
            Text(
                text = "${category?.icon.orEmpty()} ${category?.title.orEmpty()}",
                color = White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = currencySymbol + " %.2f ".format(transaction.amount),
                color = White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
