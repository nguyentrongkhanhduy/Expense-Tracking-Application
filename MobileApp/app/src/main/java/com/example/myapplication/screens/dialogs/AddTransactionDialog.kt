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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionDialog(
    viewModel: TransactionViewModel,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    categoryList: List<Category>,
    locationViewModel: LocationViewModel,
    authViewModel: AuthViewModel
) {
    val user by authViewModel.user.collectAsState()
    val typeOptions = listOf("Expense", "Income")
    var expandedType by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var expandedCategory by remember { mutableStateOf(false) }
    val filteredCategories =
        categoryList.filter { it.type.equals(viewModel.inputType, ignoreCase = true) }
    val selectedCategory = filteredCategories.find { it.categoryId == viewModel.inputCategoryId }

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    val formattedDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(
        Date(datePickerState.selectedDateMillis ?: System.currentTimeMillis())
    )

    val locationFromVM by locationViewModel.locationString.collectAsState()
    var hasUsedFetchedLocation by remember { mutableStateOf(false) }
    //reset the dialog location everytime opened
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
    val locationPermissionHandler = rememberLocationPermissionHandler(locationViewModel)

    var showImagePickerDialog by remember { mutableStateOf(false) }


    val cameraPermissionLauncher =
        rememberCameraPermissionHandler(onSuccess = { showImagePickerDialog = true })

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
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
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
                    .heightIn(min = 100.dp, max = 600.dp)
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
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryBlue
                        )
                    }
                    Text(
                        "New Transaction",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.width(40.dp)) // For symmetry with back arrow
                }

                OutlinedTextField(
                    value = viewModel.inputAmount,
                    onValueChange = {
                        viewModel.inputAmount = it
                        viewModel.validateInputs()
                    },
                    placeholder = { Text("Amount") },
                    isError = viewModel.amountError != null,
                    supportingText = { viewModel.amountError?.let { Text(it, color = Color.Red) } },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 20.sp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )

                OutlinedTextField(
                    value = viewModel.inputName,
                    onValueChange = {
                        viewModel.inputName = it
                        viewModel.validateInputs()
                    },
                    label = { Text("Name") },
                    isError = viewModel.nameError != null,
                    supportingText = { viewModel.nameError?.let { Text(it, color = Color.Red) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = !expandedType }
                ) {
                    OutlinedTextField(
                        value = viewModel.inputType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        isError = viewModel.typeError != null,
                        supportingText = {
                            viewModel.typeError?.let {
                                Text(
                                    it,
                                    color = Color.Red
                                )
                            }
                        },
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
                                    viewModel.inputType = it  // update ViewModel state
                                    viewModel.validateInputs() // optional: revalidate on change
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
                        isError = viewModel.categoryError != null,
                        supportingText = {
                            viewModel.categoryError?.let {
                                Text(
                                    it,
                                    color = Color.Red
                                )
                            }
                        },
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
                                    viewModel.inputCategoryId = it.categoryId
                                    viewModel.validateInputs()
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

                // Location field with trailing icon
                OutlinedTextField(
                    value = viewModel.inputLocation,
                    onValueChange = {
                        viewModel.inputLocation = it
                        viewModel.validateInputs()
                    },
                    label = { Text("Location") },
                    isError = viewModel.locationError != null,
                    supportingText = {
                        viewModel.locationError?.let {
                            Text(
                                it,
                                color = Color.Red
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
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

                // Upload button (optional)
//                if (user != null) {
                Button(
                    onClick = cameraPermissionLauncher,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Add photo/receipt", color = White)
                }
//                }

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

                // Cancel and Save Buttons
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
                                val transaction = Transaction(
                                    transactionId = System.currentTimeMillis(),
                                    name = viewModel.inputName,
                                    type = viewModel.inputType.lowercase(),
                                    amount = viewModel.inputAmount.toDouble(),
                                    categoryId = viewModel.inputCategoryId!!,
                                    date = viewModel.inputDate ?: System.currentTimeMillis(),
                                    note = viewModel.inputNote,
                                    location = viewModel.inputLocation,
                                    imageUrl = viewModel.inputImagePath,
                                    updatedAt = System.currentTimeMillis()
                                )
                                onSave(transaction)
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

