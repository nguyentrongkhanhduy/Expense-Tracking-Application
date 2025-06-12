package com.example.myapplication.screens

import com.example.myapplication.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.setValue
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.data.model.User
import com.example.myapplication.viewmodel.AuthViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.components.CustomBottomBar
import com.example.myapplication.data.model.TransactionWithCategory
import com.example.myapplication.helpers.removeFromInternalStorage
import com.example.myapplication.screens.dialogs.EditTransactionDialog
import com.example.myapplication.screens.dialogs.AddTransactionDialog
import com.example.myapplication.screens.tabs.AnalyticsTab
import com.example.myapplication.screens.tabs.ProfileTab
import com.example.myapplication.screens.tabs.TransactionListTab
import com.example.myapplication.viewmodel.category.CategoryViewModel
import com.example.myapplication.viewmodel.transaction.TransactionViewModel
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.ui.theme.PrimaryRed
import com.example.myapplication.ui.theme.PrimaryGreen
import com.example.myapplication.ui.theme.White
import com.example.myapplication.viewmodel.LocationViewModel
import androidx.compose.material3.FloatingActionButtonDefaults
import com.example.myapplication.components.AdBanner
import com.example.myapplication.data.datastore.UserPreferences
import com.example.myapplication.helpers.getRequestedImage
import com.example.myapplication.viewmodel.CurrencyViewModel


@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    transactionViewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel,
    locationViewModel: LocationViewModel,
    currencyViewModel: CurrencyViewModel,
    initialTab: Int = 0
) {


    val user by authViewModel.user.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionWithCategory?>(null) }

    val transactions by transactionViewModel.transactions.collectAsState()
    val transactionsWithCategory by transactionViewModel.transactionsWithCategory.collectAsState()

    val expenses = transactions.filter { it.type == "expense" }.sumOf { it.amount }
    val income = transactions.filter { it.type == "income" }.sumOf { it.amount }
    val balance = income - expenses

    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }

    val context = LocalContext.current

    var selectedCurrency by remember { mutableStateOf("Canadian Dollar") }
    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            selectedCurrency = UserPreferences.getCurrency(context)
        }
    }
    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            selectedCurrency = UserPreferences.getCurrency(context)
        }
    }
    val shortFormCurrency = currencyViewModel.getCurrencyShortForm(selectedCurrency)


    val currentBackStackEntry = navController.currentBackStackEntry
    LaunchedEffect(currentBackStackEntry) {
        val selectedTabResult = currentBackStackEntry?.savedStateHandle?.get<Int>("selectedTab")
        if (selectedTabResult != null) {
            selectedTab = selectedTabResult
            currentBackStackEntry.savedStateHandle.remove<Int>("selectedTab")
        }
    }

    Box(Modifier.fillMaxSize()) {
        // Main content
        when (selectedTab) {
            0 -> HomeTabContent(
                user = user,
                balance = "%.2f $shortFormCurrency".format(balance),
                expenses = "%.2f $shortFormCurrency".format(expenses),
                income = "%.2f $shortFormCurrency".format(income),
                transactionsWithCategory = transactionsWithCategory,
                shortFormCurrency = shortFormCurrency,
                onTransactionClick = { editingTransaction = it },

            )

            1 -> TransactionListTab(
                transactionViewModel = transactionViewModel,
                categoryViewModel = categoryViewModel,
                locationViewModel = locationViewModel,
                authViewModel = authViewModel,
                shortFormCurrency = shortFormCurrency
            )

            2 -> AnalyticsTab(
                transactionViewModel = transactionViewModel,
                currencyViewModel = currencyViewModel,
                shortFormCurrency = shortFormCurrency
            )

            3 -> ProfileTab(
                navController = navController, onLogout = {
                    authViewModel.signOut()
                    navController.navigate("login?showGuest=true") {
                        popUpTo(0)
                    }
                },
                authViewModel = authViewModel ,
                currencyViewModel = currencyViewModel
                , onCurrencyChange = { rate ->
                    transactionViewModel.updateAllAmountsByExchangeRate(rate)
                }
            )
        }

        // Custom bottom bar with cutout
        CustomBottomBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier
                .navigationBarsPadding()
                .align(Alignment.BottomCenter)

        )

        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .align(Alignment.BottomCenter)
                .size(60.dp) // Make FAB slightly larger than cutout
                .offset(y = (-32).dp) // Move FAB up to cover border
                .clip(CircleShape)
                .background(PrimaryBlue),

            contentAlignment = Alignment.Center
        ) {
            FloatingActionButton(
                onClick = {
                    transactionViewModel.resetInputFields()
                    showAddDialog = true
                },
                containerColor = PrimaryBlue,
                elevation = FloatingActionButtonDefaults.elevation(),
                shape = CircleShape,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(R.drawable.add),
                    contentDescription = "Add",
                    tint = White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            viewModel = transactionViewModel,
            onDismiss = { showAddDialog = false },
            onSave = { transaction ->
                showAddDialog = false

                transactionViewModel.addTransaction(transaction)
                transactionViewModel.checkAndNotifyBudget(context, transaction)

                if (user != null) {
                    transactionViewModel.addTransactionToFirestore(user!!.uid, transaction)

                }
            },
            categoryList = categories,
            locationViewModel = locationViewModel,
            authViewModel = authViewModel
        )
    }

    if (editingTransaction != null) {
        EditTransactionDialog(
            transaction = editingTransaction!!.transaction,
            onDismiss = { editingTransaction = null },
            onSave = { updatedTransaction ->
                transactionViewModel.updateTransaction(updatedTransaction)
                editingTransaction = null
                if (user != null) {
                    transactionViewModel.updateTransactionInFirestore(user!!.uid, updatedTransaction)
                }
                transactionViewModel.checkAndNotifyBudget(context, updatedTransaction)
            },
            onDelete = {
                val imagePath = editingTransaction!!.transaction.imageUrl
                if (imagePath?.startsWith("bitmap:") == true) {
                    removeFromInternalStorage(imagePath)
                }
                transactionViewModel.deleteTransaction(editingTransaction!!.transaction)

                if (user != null) {
                    transactionViewModel.deleteTransactionFromFirestore(user!!.uid, editingTransaction!!.transaction.transactionId)
                }

                transactionViewModel.resetInputFields()

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
fun HomeTabContent(
    user: User?,
    balance: String,
    expenses: String,
    income: String,
    shortFormCurrency: String,
    transactionsWithCategory: List<TransactionWithCategory>,
    onTransactionClick: (TransactionWithCategory) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally


    ) {
        AdBanner(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Dashboard",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 18.dp)
        )
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
                .background(PrimaryBlue, RoundedCornerShape(16.dp))
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
            shortFormCurrency = shortFormCurrency,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onTransactionClick = onTransactionClick
        )
    }
}

@Composable
fun RecentTransactionsList(
    transactions: List<TransactionWithCategory>,
    modifier: Modifier = Modifier,
    shortFormCurrency: String,
    onTransactionClick: (TransactionWithCategory) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 80.dp)
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
                        text = "%.2f ".format(transaction.amount)+ shortFormCurrency,
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}