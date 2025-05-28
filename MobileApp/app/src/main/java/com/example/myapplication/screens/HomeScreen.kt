package com.example.myapplication.screens

import com.example.myapplication.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.setValue
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.models.User
import com.example.myapplication.viewmodel.AuthViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import com.example.myapplication.data.local.model.TransactionWithCategory
import com.example.myapplication.screens.dialogs.EditTransactionDialog
import com.example.myapplication.screens.dialogs.AddTransactionDialog
import com.example.myapplication.screens.tabs.AnalyticsTab
import com.example.myapplication.screens.tabs.ProfileTab
import com.example.myapplication.screens.tabs.TransactionListTab
import com.example.myapplication.viewmodel.CategoryViewModel
import com.example.myapplication.viewmodel.TransactionViewModel
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.ui.theme.PrimaryRed
import com.example.myapplication.ui.theme.PrimaryGreen
import com.example.myapplication.ui.theme.White

@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    transactionViewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel,
    isGuest: Boolean = false
) {
    val user by authViewModel.user.collectAsState()
    val isSignedIn by authViewModel.isSignedIn.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionWithCategory?>(null) }

    val transactions by transactionViewModel.transactions.collectAsState()
    val transactionsWithCategory by transactionViewModel.transactionsWithCategory.collectAsState()

    val expenses = transactions.filter { it.type == "expense" }.sumOf { it.amount }
    val income = transactions.filter { it.type == "income" }.sumOf { it.amount }
    val balance = income - expenses

    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(isSignedIn) {
        if (!isSignedIn && !isGuest) {
            navController.navigate("login?showGuest=true") {
                popUpTo(0)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = PrimaryBlue
                ) {
                    Icon(
                        painter = painterResource(R.drawable.add),
                        contentDescription = "Add",
                        tint = White
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        bottomBar = {
            BottomNavigationBar(selectedTab) { selectedTab = it }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            when (selectedTab) {
                0 -> HomeTabContent(
                    user = user,
                    balance = "$%.2f".format(balance),
                    expenses = "$%.2f".format(expenses),
                    income = "$%.2f".format(income),
                    transactionsWithCategory = transactionsWithCategory,
                    onTransactionClick = { editingTransaction = it }
                )

                1 -> TransactionListTab(
                    transactionViewModel = transactionViewModel,
                    categoryViewModel = categoryViewModel
                )
                2 -> AnalyticsTab()
                3 -> ProfileTab(navController = navController)
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onSave = {
                transactionViewModel.addTransaction(it)
                showAddDialog = false
            },
            categoryList = categories
        )
    }

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
fun HomeTabContent(
    user: User?,
    balance: String,
    expenses: String,
    income: String,
    transactionsWithCategory: List<TransactionWithCategory>,
    onTransactionClick: (TransactionWithCategory) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 24.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Balance Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryBlue, RoundedCornerShape(20.dp))
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${user?.displayName?.let { "$it's" } ?: "Your"} Balance",
                    color = White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = balance,
                    color = White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryBlue.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Overview",
                    color = White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Expenses", color = White)
                        Text(
                            text = expenses,
                            color = PrimaryRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Income", color = White)
                        Text(
                            text = income,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Recent Transactions",
            color = PrimaryBlue,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(Modifier.height(8.dp))

        RecentTransactionsList(
            transactions = transactionsWithCategory,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 72.dp * 6),
            onTransactionClick = onTransactionClick
        )
    }
}

@Composable
fun RecentTransactionsList(
    transactions: List<TransactionWithCategory>,
    modifier: Modifier = Modifier,
    onTransactionClick: (TransactionWithCategory) -> Unit
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(transactions) { transactionWithCategory ->
            val transaction = transactionWithCategory.transaction
            val category = transactionWithCategory.category
            val isIncome = transaction.type == "income"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(
                        if (isIncome) PrimaryGreen else PrimaryRed,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
                    .clickable { onTransactionClick(transactionWithCategory) }
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
    }
}

@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = {
                Text(
                    "Home",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painterResource(R.drawable.list),
                    contentDescription = "Transaction List"
                )
            },
            label = {
                Text(
                    "Transaction list",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) }
        )
        NavigationBarItem(
            icon = { Icon(painterResource(R.drawable.barchart), contentDescription = "Analytics") },
            label = {
                Text(
                    "Analytics",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = {
                Text(
                    "Profile",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) }
        )
    }
}
