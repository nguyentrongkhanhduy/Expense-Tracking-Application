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
import com.example.myapplication.data.local.model.TransactionWithCategory
import com.example.myapplication.screens.dialogs.EditTransactionDialog
import com.example.myapplication.viewmodel.CategoryViewModel
import com.example.myapplication.viewmodel.TransactionViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.components.CustomSegmentedTabRow
import com.example.myapplication.helpers.removeFromInternalStorage
import com.example.myapplication.screens.dialogs.AddTransactionDialog
import com.example.myapplication.screens.dialogs.CustomCategoryFilterDialog
import com.example.myapplication.screens.dialogs.CustomDateRangeDialog
import com.example.myapplication.ui.theme.PrimaryGreen
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.LocationViewModel
import java.util.Calendar

@Composable
fun TransactionListTab(
    transactionViewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel,
    locationViewModel: LocationViewModel,
    authViewModel: AuthViewModel
) {
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .padding(horizontal = 24.dp, vertical = 18.dp)
        ) {
            // Title
            Text(
                text = "Transactions",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 18.dp)
            )

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
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredTransactions) { transactionWithCategory ->
                    TransactionCard(
                        transactionWithCategory = transactionWithCategory,
                        onClick = { editingTransaction = transactionWithCategory }
                    )
                }
            }

        }

        // --- AddTransactionDialog ---
        if (showAddDialog) {
            AddTransactionDialog(
                viewModel = transactionViewModel,
                onDismiss = { showAddDialog = false },
                onSave = {
                    transactionViewModel.addTransaction(it)
                    showAddDialog = false
                    transactionViewModel.checkAndNotifyBudget(context, it)
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
                onSave = { updatedTransaction ->
                    transactionViewModel.updateTransaction(updatedTransaction)
                    editingTransaction = null
                    transactionViewModel.checkAndNotifyBudget(context, updatedTransaction)
                },
                onDelete = {
                    val imagePath = editingTransaction!!.transaction.imageUrl
                    if (imagePath?.startsWith("bitmap:") == true) {
                        removeFromInternalStorage(imagePath)
                    }
                    transactionViewModel.deleteTransaction(editingTransaction!!.transaction)
                    editingTransaction = null
                },
                categoryList = categories,
                locationViewModel = locationViewModel,
                viewModel = transactionViewModel,
            )
        }
    }
}

@Composable
fun TransactionCard(
    transactionWithCategory: TransactionWithCategory,
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
                val formattedDate = java.text.SimpleDateFormat("MM/dd/yyyy")
                    .format(java.util.Date(transaction.date))
                Text(formattedDate, color = White, fontWeight = FontWeight.SemiBold)
                Text(transaction.name, color = White)
            }
            Text(
                text = "${category?.icon.orEmpty()} ${category?.title.orEmpty()}",
                color = White,
                fontWeight = FontWeight.Bold
            )
            Text(
                transaction.amount.toString(),
                color = White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

