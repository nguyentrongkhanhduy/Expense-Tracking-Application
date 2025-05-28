package com.example.myapplication.screens

import com.example.myapplication.R
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.models.User
import com.example.myapplication.viewmodel.AuthViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton

data class Transaction(
    val date: String,
    val name: String,
    val amount: String,
    val type: String
)

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val user by viewModel.user.collectAsState()
    val isSignedIn by viewModel.isSignedIn.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // Example transaction list (replace with your data source)
    val transactions = listOf(
        Transaction("03/03/2024", "Apple Airpods", "299.99", "expense"),
        Transaction("03/01/2024", "Apples", "11.99", "expense"),
        Transaction("02/26/2024", "Pay Check", "181.72", "income"),
        Transaction("02/22/2024", "Tea Leaves", "5.99", "expense"),
        Transaction("02/21/2024", "Groceries", "45.00", "expense"),
        Transaction("02/20/2024", "Gift", "50.00", "income"),
        Transaction("02/19/2024", "Coffee", "3.50", "expense"),
    )

    val balance = "$2500.00"
    val expenses = "$1000.00"
    val income = "$300.00"

    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(isSignedIn) {
        if (!isSignedIn) {
            navController.navigate("login?showGuest=true") {
                popUpTo(0)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            // FAB only on Home tab
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Color(0xFF1C3556)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.add),
                        contentDescription = "Add",
                        tint = Color.White
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            BottomNavigationBar(selectedTab) { selectedTab = it }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            when (selectedTab) {
                0 -> HomeTabContent(
                    user = user,
                    balance = balance,
                    expenses = expenses,
                    income = income,
                    transactions = transactions
                )
                1 -> TransactionListTab()
                2 -> AnalyticsTab()
                3 -> ProfileTab(navController = navController)
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { /* handle save */ showAddDialog = false }
        )
    }
}


@Composable
fun HomeTabContent(
    user: User?,
    balance: String,
    expenses: String,
    income: String,
    transactions: List<Transaction>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Balance Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1C3556), RoundedCornerShape(20.dp))
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${user?.displayName ?: "David"}'s Balance",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = balance,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2E4667), RoundedCornerShape(16.dp))
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Overview",
                    color = Color.White,
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
                        Text("Expenses", color = Color.White)
                        Text(
                            text = expenses,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Income", color = Color.White)
                        Text(
                            text = income,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }


        Spacer(Modifier.height(20.dp))

        // Recent Transactions label
        Text(
            "Recent Transactions",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(Modifier.height(8.dp))

        RecentTransactionsList(
            transactions = transactions,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

@Composable
fun RecentTransactionsList(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {

    LazyColumn(
        modifier = modifier
    ) {
        items(transactions.take(5)) { transaction ->
            val isIncome = transaction.type == "income"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(
                        if (isIncome) Color(0xFF4CAF50) else Color(0xFFFF5252),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(transaction.date, color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(transaction.name, color = Color.White)
                    }
                    Text(
                        transaction.amount,
                        color = Color.White,
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
        containerColor = Color.White,
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



