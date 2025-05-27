package com.example.myapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.data.local.repository.RepositoryProvider
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.CategoryViewModel

@Composable
fun HomeScreen(navController: NavController, viewModel: AuthViewModel, isGuest: Boolean = false) {
    val user by viewModel.user.collectAsState()
    val isSignedIn by viewModel.isSignedIn.collectAsState()

    val context = LocalContext.current
    val categoryViewModel = remember {
        CategoryViewModel(RepositoryProvider.getCategoryRepository(context))
    }

    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isSignedIn) {
        if (!isSignedIn && !isGuest) {
            navController.navigate("login?showGuest=true") {
                popUpTo(0)
            }
        }
    }

    LaunchedEffect(Unit) {
        categoryViewModel.printCategories()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (user != null) {
                Text(
                    text = user!!.displayName,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { viewModel.signOut() },
                ) {
                    Text("Log out")
                }
            } else {
                Text("Guest mode (Local only)")
                if (showDialog) {
                    AddCategoryDialog(
                        onDismiss = { showDialog = false },
                        onSave = { type, title, icon, limit ->
                            categoryViewModel.addCategory(
                                com.example.myapplication.data.local.model.Category(
                                    title = title,
                                    icon = icon,
                                    type = type,
                                    limit = limit?.toDoubleOrNull()
                                )
                            )
                        }
                    )
                }
                Button(onClick = { showDialog = true }) {
                    Text("Add new Category")
                }
                Button(onClick = {}) {
                    Text("Add new Transaction")
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String?) -> Unit
) {
    var type by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("expense", "income")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                onSave(type, title, icon, limit.takeIf { it.isNotBlank() })
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Cancel")
            }
        },
        title = { Text("New category", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        options.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    type = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = icon, onValueChange = { icon = it }, label = { Text("Icon") })
                OutlinedTextField(
                    value = limit,
                    onValueChange = { limit = it },
                    label = { Text("Limit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    )
}


