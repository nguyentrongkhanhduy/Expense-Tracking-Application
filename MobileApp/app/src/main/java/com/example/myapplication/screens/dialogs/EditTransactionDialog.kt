package com.example.myapplication.screens.dialogs

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.data.model.Category
import com.example.myapplication.data.model.Transaction
import com.example.myapplication.helpers.loadImageUriOrBitmapFromInternalStorage
import com.example.myapplication.helpers.rememberCameraPermissionHandler
import com.example.myapplication.helpers.rememberLocationPermissionHandler
import com.example.myapplication.helpers.saveBitmapToInternalStorage
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.ui.theme.PrimaryRed
import com.example.myapplication.ui.theme.White
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.LocationViewModel
import com.example.myapplication.viewmodel.transaction.TransactionViewModel
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
    authViewModel: AuthViewModel
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

    val filteredCategories =
        categoryList.filter { it.type.equals(viewModel.inputType, ignoreCase = true) }
    val selectedCategory = filteredCategories.find { it.categoryId == viewModel.inputCategoryId }

    // Date picker state
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = viewModel.inputDate)
    var showDatePicker by remember { mutableStateOf(false) }
    val utcFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val formattedDate = utcFormat.format(
    Date(viewModel.inputDate ?: System.currentTimeMillis())
    )

    // Location logic
    val locationFromVM by locationViewModel.locationString.collectAsState()
    val locationPermissionHandler = rememberLocationPermissionHandler(locationViewModel)
    var hasUsedFetchedLocation by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        locationViewModel.clearLocation()
        hasUsedFetchedLocation = false
        onDispose { }
    }

    LaunchedEffect(locationFromVM, hasUsedFetchedLocation) {
        if (!locationFromVM.isNullOrEmpty() && !hasUsedFetchedLocation) {
            viewModel.inputLocation = locationFromVM.toString()
            viewModel.validateInputs()
            hasUsedFetchedLocation = true
        }
    }


    val context = LocalContext.current
    var showImagePickerDialog by remember { mutableStateOf(false) }
    val cameraPermissionLauncher =
        rememberCameraPermissionHandler(onSuccess = { showImagePickerDialog = true })

    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(viewModel.inputImagePath) {
        if (transaction.imageUrl != null) {
            val image =
                loadImageUriOrBitmapFromInternalStorage(context, viewModel.inputImagePath ?: "")
            selectedImageBitmap = image as? Bitmap
            selectedImageUri = image as? Uri
        }
    }


    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            if (bitmap != null) {
                val path = bitmap.let {
                    saveBitmapToInternalStorage(
                        context,
                        it
                    )?.let { saved -> "bitmap:$saved" }
                }
                selectedImageBitmap = bitmap
                selectedImageUri = null
                viewModel.inputImagePath = path
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                val path = uri.toString().let { "uri:$it" }
                selectedImageUri = uri
                selectedImageBitmap = null
                viewModel.inputImagePath = path
            }
        }
    )

    if (showImagePickerDialog) {
        ImagePickerDialog(
            onDismiss = { showImagePickerDialog = false },
            context = context,
            cameraLauncher = cameraLauncher,
            galleryLauncher = galleryLauncher
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .background(White)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Bar with Back and Delete
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
                        "Edit Transaction",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    IconButton(
                        onClick = {
                            onDelete()
                            onDismiss()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Transaction",
                            tint = PrimaryRed
                        )
                    }
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
                        supportingText = {
                            viewModel.typeError?.let {
                                Text(
                                    it,
                                    color = Color.Red
                                )
                            }
                        },
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
                        value = selectedCategory?.let { "${it.icon} ${it.title}" }
                            ?: "Select Category",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        isError = viewModel.categoryError != null,
                        supportingText = {
                            viewModel.categoryError?.let {
                                Text(
                                    it,
                                    color = Color.Red
                                )
                            }
                        },
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
                    supportingText = {
                        viewModel.locationError?.let {
                            Text(
                                it,
                                color = Color.Red
                            )
                        }
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                hasUsedFetchedLocation = false
                                locationPermissionHandler()
                            }
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Pick location")
                        }
                    }
                )

                Button(
                    onClick = cameraPermissionLauncher,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Add photo/receipt", color = White)
                }

                selectedImageBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                selectedImageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                // Action Buttons
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
                            if (viewModel.validateInputs()) {
                                val currentTime = System.currentTimeMillis()
                                val updatedTransaction = transaction.copy(
                                    name = viewModel.inputName,
                                    type = viewModel.inputType.lowercase(),
                                    amount = viewModel.inputAmount.toDoubleOrNull() ?: 0.0,
                                    categoryId = viewModel.inputCategoryId
                                        ?: transaction.categoryId,
                                    date = viewModel.inputDate ?: transaction.date,
                                    note = viewModel.inputNote,
                                    location = viewModel.inputLocation,
                                    imageUrl = viewModel.inputImagePath,
                                    updatedAt = currentTime
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
