package com.example.myapplication.screens.dialogs

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.myapplication.data.local.model.Category
import com.example.myapplication.data.local.model.Transaction
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.ui.theme.PrimaryRed
import com.example.myapplication.ui.theme.White
import com.example.myapplication.viewmodel.LocationViewModel
import com.example.myapplication.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    viewModel: TransactionViewModel,
    transaction: Transaction,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    onDelete: () -> Unit,
    categoryList: List<Category>,
    locationViewModel: LocationViewModel,
) {
    // Prefill ViewModel state ONCE per transaction
    LaunchedEffect(transaction.transactionId) {
        viewModel.resetInputFields(
            amount = transaction.amount.toString(),
            name = transaction.name,
            type = transaction.type.replaceFirstChar { it.uppercase() },
            categoryId = transaction.categoryId,
            note = transaction.note ?: "",
            date = transaction.date,
            location = transaction.location ?: "",
            imagePath = transaction.imageUrl
        )
    }

    val typeOptions = listOf("Expense", "Income")
    var expandedType by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }

    val filteredCategories = categoryList.filter { it.type.equals(viewModel.inputType, ignoreCase = true) }
    val selectedCategory = filteredCategories.find { it.categoryId == viewModel.inputCategoryId }

    // Date picker state
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = viewModel.inputDate)
    var showDatePicker by remember { mutableStateOf(false) }
    val formattedDate = remember(viewModel.inputDate) {
        SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(
            Date(viewModel.inputDate ?: System.currentTimeMillis())
        )
    }

    // Location logic
    val locationFromVM by locationViewModel.locationString.collectAsState()
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                locationViewModel.fetchLocation()
            } else {
                Toast.makeText(
                    context,
                    "Permission denied. Please enable location in settings.",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                context.startActivity(intent)
            }
        }
    )
    LaunchedEffect(locationFromVM) {
        if (!locationFromVM.isNullOrEmpty()) {
            viewModel.inputLocation = locationFromVM.toString()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .imePadding()
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
                    Spacer(Modifier.width(40.dp))
                }

                OutlinedTextField(
                    value = viewModel.inputAmount,
                    onValueChange = {
                        viewModel.inputAmount = it
                        viewModel.validateInputs()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Amount") },
                    textStyle = TextStyle(fontSize = 20.sp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = viewModel.amountError != null,
                    supportingText = { viewModel.amountError?.let { Text(it, color = Color.Red) } }
                )

                OutlinedTextField(
                    value = viewModel.inputName,
                    onValueChange = {
                        viewModel.inputName = it
                        viewModel.validateInputs()
                    },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = viewModel.nameError != null,
                    supportingText = { viewModel.nameError?.let { Text(it, color = Color.Red) } }
                )

                // Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = !expandedType }
                ) {
                    OutlinedTextField(
                        value = viewModel.inputType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                        isError = viewModel.typeError != null,
                        supportingText = { viewModel.typeError?.let { Text(it, color = Color.Red) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        typeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    if (viewModel.inputType != option) {
                                        viewModel.inputType = option
                                        viewModel.inputCategoryId = null // Reset category!
                                        viewModel.validateInputs()
                                    }
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
                        isError = viewModel.categoryError != null,
                        supportingText = { viewModel.categoryError?.let { Text(it, color = Color.Red) } },
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
                                    viewModel.inputCategoryId = it.categoryId
                                    viewModel.validateInputs()
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = viewModel.inputNote,
                    onValueChange = { viewModel.inputNote = it },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

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
                            TextButton(onClick = {
                                showDatePicker = false
                                viewModel.inputDate = datePickerState.selectedDateMillis
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                OutlinedTextField(
                    value = viewModel.inputLocation,
                    onValueChange = {
                        viewModel.inputLocation = it
                        viewModel.validateInputs()
                    },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = viewModel.locationError != null,
                    supportingText = { viewModel.locationError?.let { Text(it, color = Color.Red) } },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(
                                        context, android.Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    locationViewModel.fetchLocation()
                                } else {
                                    launcher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            }
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Pick location")
                        }
                    }
                )


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
                            if (viewModel.validateInputs()) {
                                val updatedTransaction = transaction.copy(
                                    name = viewModel.inputName,
                                    type = viewModel.inputType.lowercase(),
                                    amount = viewModel.inputAmount.toDoubleOrNull() ?: 0.0,
                                    categoryId = viewModel.inputCategoryId ?: transaction.categoryId,
                                    date = viewModel.inputDate ?: transaction.date,
                                    note = viewModel.inputNote,
                                    location = viewModel.inputLocation
                                )
                                onSave(updatedTransaction)
                                viewModel.resetInputFields()
                                onDismiss()
                            }
                        },
                        enabled = viewModel.validateInputs(),
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
