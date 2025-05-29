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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.ui.theme.ButtonBlue
import com.example.myapplication.ui.theme.PrimaryGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@Composable
fun TransactionListTab(
    transactionViewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel
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
                // optionally trigger filter logic here
            }
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
                    onClick = {},
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

    // Edit dialog
    if (editingTransaction != null) {
        EditTransactionDialog(
            transaction = editingTransaction!!.transaction,
            onDismiss = { editingTransaction = null },
            onSave = { updatedTransaction ->
                transactionViewModel.updateTransaction(updatedTransaction)
                editingTransaction = null
            },
            onDelete = {
                transactionViewModel.deleteTransaction(editingTransaction!!.transaction)
                editingTransaction = null
            },
            categoryList = categories
        )
    }
}


@Composable
fun CustomSegmentedTabRow(
    tabTexts: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color(0xFFE5E7EB), RoundedCornerShape(20.dp)),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        tabTexts.forEachIndexed { idx, txt ->
            Button(
                onClick = { onTabSelected(idx) },
                shape = when (idx) {
                    0 -> RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                    tabTexts.lastIndex -> RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
                    else -> RoundedCornerShape(0.dp)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == idx) PrimaryBlue else Color.Transparent,
                    contentColor = if (selectedTab == idx) White else PrimaryBlue
                ),
                border = if (selectedTab == idx) null else ButtonDefaults.outlinedButtonBorder,
                elevation = null,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = txt,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = if (selectedTab == idx) White else PrimaryBlue
                )
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateRangeDialog(
    onDismiss: () -> Unit,
    onConfirm: (start: Long, end: Long) -> Unit
) {
    val startDateState = rememberDatePickerState()
    val endDateState = rememberDatePickerState()
    val formattedStartDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(
        Date(startDateState.selectedDateMillis ?: System.currentTimeMillis())
    )
    val formattedEndDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(
        Date(endDateState.selectedDateMillis ?: System.currentTimeMillis())
    )

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        ),

        ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(0.97f)
                .wrapContentHeight()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .background(White)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryBlue
                        )
                    }
                    Text(
                        "Custom Date",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.width(40.dp))
                }

                OutlinedTextField(
                    value = formattedStartDate,
                    onValueChange = {},
                    label = { Text("Date") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showStartPicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pick date")
                        }
                    }
                )
                if (showStartPicker) {
                    DatePickerDialog(
                        onDismissRequest = { showStartPicker = false },
                        confirmButton = {
                            TextButton(onClick = { showStartPicker = false }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showStartPicker = false }) { Text("Cancel") }
                        }
                    ) {
                        DatePicker(state = startDateState)
                    }
                }

                OutlinedTextField(
                    value = formattedEndDate,
                    onValueChange = {},
                    label = { Text("Date") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showEndPicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pick date")
                        }
                    }
                )
                if (showEndPicker) {
                    DatePickerDialog(
                        onDismissRequest = { showEndPicker = false },
                        confirmButton = {
                            TextButton(onClick = { showEndPicker = false }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEndPicker = false }) { Text("Cancel") }
                        }
                    ) {
                        DatePicker(state = endDateState)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(4.dp)
                    ) {
                        Text("Cancel", color = White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            onConfirm(
                                startDateState.selectedDateMillis!!,
                                endDateState.selectedDateMillis!!
                            )
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonBlue),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(4.dp)
                    ) {
                        Text("Confirm", color = White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}