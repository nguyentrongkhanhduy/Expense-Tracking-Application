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
import com.example.myapplication.data.local.model.Transaction
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.ui.theme.PrimaryRed
import com.example.myapplication.ui.theme.White
import com.example.myapplication.R
import com.example.myapplication.data.local.model.TransactionWithCategory
import com.example.myapplication.screens.dialogs.EditTransactionDialog
import com.example.myapplication.viewmodel.CategoryViewModel
import com.example.myapplication.viewmodel.TransactionViewModel
import androidx.compose.foundation.lazy.items
import com.example.myapplication.ui.theme.PrimaryGreen


@Composable
fun TransactionListTab(
    transactionViewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("Google")) }
    val tabTexts = listOf("All", "Today", "Week", "Month", "Custom")
    var selectedTab by remember { mutableIntStateOf(0) }
    val transactionsWithCategory by transactionViewModel.transactionsWithCategory.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()
    var editingTransaction by remember { mutableStateOf<TransactionWithCategory?>(null) }

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
            placeholder = { Text("Search") },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
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
                    onClick = {},
                    backgroundColor = PrimaryBlue
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.sort),
                        contentDescription = "Sort",
                        tint = White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sort",
                        color = White,
                        fontWeight = FontWeight.Medium
                    )
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
            items(transactionsWithCategory) { transactionWithCategory ->
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
            .padding(vertical = 4.dp)
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
