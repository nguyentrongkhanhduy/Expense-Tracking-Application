package com.example.myapplication.screens.dialogs
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.data.local.model.Category
import com.example.myapplication.data.local.model.Transaction
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    categoryList: List<Category>
) {
    var amount by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Expense") }
    val typeOptions = listOf("Expense", "Income")
    var expandedType by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    var categoryId by remember { mutableStateOf<Long?>(null) }
    var expandedCategory by remember { mutableStateOf(false) }
    val filteredCategories = categoryList.filter { it.type.equals(type, ignoreCase = true) }
    val selectedCategory = filteredCategories.find { it.categoryId == categoryId }

    // Date picker state
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    val formattedDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(
        Date(datePickerState.selectedDateMillis ?: System.currentTimeMillis())
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
                    .background(Color.White)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF1C3556))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "New Transaction",
                        color = Color(0xFF1C3556),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1C3556), RoundedCornerShape(28.dp)),
                    placeholder = { Text("Amount", color = Color.White) },
                    textStyle = TextStyle(color = Color.White, fontSize = 20.sp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedContainerColor = Color(0xFF1C3556),
                        unfocusedContainerColor = Color(0xFF1C3556)
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Name",
                        color = Color(0xFF1C3556),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(70.dp)
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF1C3556), RoundedCornerShape(28.dp)),
                        textStyle = TextStyle(color = Color.White),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color.White,
                            focusedContainerColor = Color(0xFF1C3556),
                            unfocusedContainerColor = Color(0xFF1C3556)
                        )
                    )
                }

                // Type Row (label beside dropdown)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Type",
                        color = Color(0xFF1C3556),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(70.dp)
                    )
                    ExposedDropdownMenuBox(
                        expanded = expandedType,
                        onExpandedChange = { expandedType = !expandedType }
                    ) {
                        OutlinedTextField(
                            value = type,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .weight(1f)
                                .menuAnchor()
                                .background(
                                    if (type == "Expense") Color(0xFFFF5252) else Color(0xFF4CAF50),
                                    RoundedCornerShape(28.dp)
                                ),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType)
                            },
                            textStyle = TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = Color.White,
                                focusedContainerColor = if (type == "Expense") Color(0xFFFF5252) else Color(0xFF4CAF50),
                                unfocusedContainerColor = if (type == "Expense") Color(0xFFFF5252) else Color(0xFF4CAF50)
                            )
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
                }

                // Category dropdown (full width)
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.let { "${it.icon} ${it.title}" } ?: "Select Category",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .background(Color(0xFF1C3556), RoundedCornerShape(28.dp)),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color.White,
                            focusedContainerColor = Color(0xFF1C3556),
                            unfocusedContainerColor = Color(0xFF1C3556)
                        )
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

                // Note field (full width)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(Color(0xFF1C3556), RoundedCornerShape(28.dp)),
                    placeholder = { Text("Note", color = Color.White) },
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedContainerColor = Color(0xFF1C3556),
                        unfocusedContainerColor = Color(0xFF1C3556)
                    )
                )

                // Date field (full width, calendar)
                OutlinedTextField(
                    value = formattedDate,
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1C3556), RoundedCornerShape(28.dp)),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Pick date", tint = Color.White)
                        }
                    },
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedContainerColor = Color(0xFF1C3556),
                        unfocusedContainerColor = Color(0xFF1C3556)
                    )
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF1C3556), RoundedCornerShape(28.dp)),
                        placeholder = { Text("Location", color = Color.White) },
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color.White,
                            focusedContainerColor = Color(0xFF1C3556),
                            unfocusedContainerColor = Color(0xFF1C3556)
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { /* handle location */ },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF1C3556), CircleShape)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Pick location", tint = Color.White)
                    }
                }

                // Upload button
                Button(
                    onClick = { /* handle upload */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1C3556), RoundedCornerShape(28.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C3556)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Upload photo/receipt", color = Color.White)
                }

                // Cancel and Save Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text("Cancel", color = Color.White)
                    }
                    Button(
                        onClick = {
                            val transaction = Transaction(
                                name = name,
                                type = type.lowercase(),
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                categoryId = categoryId ?: if (type == "expense") -1L else -2L,
                                date = datePickerState.selectedDateMillis ?: System.currentTimeMillis(),
                                note = note,
                                location = location,
                                imageUrl = null // Add image URL support if available
                            )
                            onSave(transaction)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text("Save", color = Color.White)
                    }
                }
            }
        }
    }
}
