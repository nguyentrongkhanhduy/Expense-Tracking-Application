package com.example.myapplication.screens.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.components.CustomDropdownMenu
import com.example.myapplication.components.MyButton
import com.example.myapplication.data.datastore.UserPreferences
import com.example.myapplication.screens.dialogs.ConfirmationDialog
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.ui.theme.PrimaryRed
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.CurrencyViewModel

@Composable
fun ProfileTab(
    navController: NavController,
    currencyViewModel: CurrencyViewModel,
    authViewModel: AuthViewModel,
    onCurrencyChange: (Double) -> Unit = {},
    onSyncData: () -> Unit = {},
    onClearData: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val context = LocalContext.current

    val user by authViewModel.user.collectAsState()
    val displayName = user?.displayName ?: "Username"
    val email = user?.email ?: "Username@gmail.com"

    val currencyOptions = listOf(
        "US Dollar",
        "Canadian Dollar",
        "Australian Dollar",
        "Indian Rupee",
        "Euro",
        "British Pound",
        "Japanese Yen",
        "Chinese Yuan",
        "Swiss Franc",
        "Swedish Krona",
        "Hong Kong Dollar",
        "Singapore Dollar",
        "Russian Ruble"
    )


    var selectedCurrency by remember { mutableStateOf("") }

    var logOutDialog by remember { mutableStateOf(false) }
    var clearDataDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val defaultCurrency = UserPreferences.getCurrency(context)
        selectedCurrency = defaultCurrency
    }

    LaunchedEffect(selectedCurrency) {
        UserPreferences.setCurrency(context, selectedCurrency) // Persist the new selection
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Profile",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0A2540),
            modifier = Modifier.padding(bottom = 18.dp)
        )
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(6.dp, Color(0xFF222B45), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Image",
                tint = Color(0xFF222B45),
                modifier = Modifier.size(100.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            if (user != null) displayName else "Guest",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        if (user != null) {
            Text(
                text = email,
                fontSize = 16.sp,
                color = Color(0xFF222B45)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Currency preference:",
                fontSize = 15.sp
            )
            CustomDropdownMenu(
                list = currencyOptions,
                selected = selectedCurrency,
                color = PrimaryBlue,
                onSelected = { selectedIndex ->
                    val fromCurrency = currencyViewModel.getCurrencyShortForm(selectedCurrency)
                    val toCurrency = currencyViewModel.getCurrencyShortForm(currencyOptions[selectedIndex])
                    selectedCurrency = currencyOptions[selectedIndex]

                    currencyViewModel.getExchangeRate(fromCurrency, toCurrency) {
                        it.quotes.entries.firstOrNull()?.value?.let { rate ->
                            println("DEBUG: exchange rate = $rate")
                            onCurrencyChange(rate)
                        }
                    }
                },
                modifier = Modifier.padding(start = 10.dp),
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        MyButton(onClick = { navController.navigate("categories?fromTab=3") }) {
            Text(
                "Manage categories",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        if (user != null) {
            MyButton(onClick = onSyncData) {
                Text(
                    "Sync data",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            MyButton(onClick = {
                logOutDialog = true
            }, backgroundColor = PrimaryRed) {
                Text("Log out", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            MyButton(onClick = { navController.navigate("login?showGuest=true") }) {
                Text("Log in", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(32.dp))

        if (logOutDialog) {
            ConfirmationDialog(
                title = "Log out",
                message = "Are you sure you want to log out?",
                onConfirm = {
                    clearDataDialog = true
                    logOutDialog = false
                },
                onCancel = {
                    logOutDialog = false
                },
                onDismiss = {
                    logOutDialog = false
                }
            )
        }

        if (clearDataDialog) {
            ConfirmationDialog(
                title = "Clear data",
                message = "Do you want to clear all data?",
                onConfirm = {
                    onLogout()
                    onClearData()
                    clearDataDialog = false
                },
                onCancel = {
                    onLogout()
                    clearDataDialog = false
                },
                onDismiss = {
                    clearDataDialog = false
                }
            )
        }
    }
}
