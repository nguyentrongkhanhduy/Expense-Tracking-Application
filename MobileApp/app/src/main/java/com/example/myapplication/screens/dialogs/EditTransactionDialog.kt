package com.example.myapplication.screens.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.data.local.model.Category
import com.example.myapplication.data.local.model.Transaction
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.ui.theme.PrimaryRed
import com.example.myapplication.ui.theme.White
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    onDelete: () -> Unit,
    categoryList: List<Category>
) {
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var name by remember { mutableStateOf(transaction.name) }
    var type by remember { mutableStateOf(transaction.type.replaceFirstChar { it.uppercase() }) }
    val typeOptions = listOf("Expense", "Income")
    var expandedType by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf(transaction.note ?: "") }
    var location by remember { mutableStateOf(transaction.location ?: "") }

    var categoryId by remember { mutableStateOf<Long?>(transaction.categoryId) }
    var expandedCategory by remember { mutableStateOf(false) }
    val filteredCategories = categoryList.filter { it.type.equals(type, ignoreCase = true) }
    val selectedCategory = filteredCategories.find { it.categoryId == categoryId }

    // Date picker state
    val initialMillis = transaction.date
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    var showDatePicker by remember { mutableStateOf(false) }
    val formattedDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(
        Date(datePickerState.selectedDateMillis ?: initialMillis)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .background(White)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryBlue)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Edit Transaction",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.width(40.dp)) // To balance the back arrow
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Amount") },
                    textStyle = TextStyle(fontSize = 20.sp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = !expandedType }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        typeOptions.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    type = it
                                    expandedType = false
                                }
                            )
                        }
                    }
                }

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.let { "${it.icon} ${it.title}" } ?: "Select Category",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        filteredCategories.forEach {
                            DropdownMenuItem(
                                text = { Text("${it.icon} ${it.title}") },
                                onClick = {
                                    categoryId = it.categoryId
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Date field with calendar icon
                OutlinedTextField(
                    value = formattedDate,
                    onValueChange = {},
                    label = { Text("Date") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pick date")
                        }
                    }
                )
                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                // Location field with trailing icon
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(
                            onClick = { /* handle location */ }
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Pick location")
                        }
                    }
                )

                // Upload button (optional)
                Button(
                    onClick = { /* handle upload */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Upload photo/receipt", color = White)
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            onDelete()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(4.dp)
                    ) {
                        Text("Delete", color = White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val updatedTransaction = transaction.copy(
                                name = name,
                                type = type.lowercase(),
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                categoryId = categoryId ?: transaction.categoryId,
                                date = datePickerState.selectedDateMillis ?: transaction.date,
                                note = note,
                                location = location
                            )
                            onSave(updatedTransaction)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(4.dp)
                    ) {
                        Text("Save", color = White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
