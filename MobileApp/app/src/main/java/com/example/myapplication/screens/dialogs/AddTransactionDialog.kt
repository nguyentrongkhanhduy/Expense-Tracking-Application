package com.example.myapplication.screens.dialogs

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
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
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.data.local.model.Category
import com.example.myapplication.data.local.model.Transaction
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.ui.theme.PrimaryRed
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.theme.ButtonBlue
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.LocationViewModel
import com.example.myapplication.viewmodel.TransactionViewModel
import java.io.File
import java.io.FileOutputStream
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

//    var amount by remember { mutableStateOf("") }
//    var name by remember { mutableStateOf("") }
//    var type by remember { mutableStateOf("Expense") }
    val typeOptions = listOf("Expense", "Income")
    var expandedType by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imagePath by remember { mutableStateOf<String?>(null) }

//    var categoryId by remember { mutableStateOf<Long?>(null) }
    var expandedCategory by remember { mutableStateOf(false) }
    val filteredCategories = categoryList.filter { it.type.equals(viewModel.inputType, ignoreCase = true) }
    val selectedCategory = filteredCategories.find { it.categoryId == viewModel.inputCategoryId }

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    val formattedDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(
        Date(datePickerState.selectedDateMillis ?: System.currentTimeMillis())
    )

    val locationFromVM by locationViewModel.locationString.collectAsState()
    var manualLocation by remember { mutableStateOf("") }
    var hasUsedFetchedLocation by remember { mutableStateOf(false) }
    //reset the dialog location everytime opened
    DisposableEffect(Unit) {
        locationViewModel.clearLocation()
        manualLocation = ""
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

    var showImagePickerDialog by remember { mutableStateOf(false) }
    val cameraPermission = android.Manifest.permission.CAMERA


    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            val path = bitmap?.let { saveBitmapToInternalStorage(context, it) }
            selectedImageBitmap = bitmap
            selectedImageUri = null
            imagePath = path
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            selectedImageUri = uri
            selectedImageBitmap = null
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                showImagePickerDialog = true
            } else {
                Toast.makeText(
                    context,
                    "Camera permission denied. Please enable it in settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    )

    if (showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            confirmButton = {},
            dismissButton = {},
            containerColor = Color.White,
            title = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Upload Photo/Receipt", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = {
                            cameraLauncher.launch(null)
                            showImagePickerDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Take a Photo", color = Color.White)
                    }
                    Button(
                        onClick = {
                            galleryLauncher.launch("image/*")
                            showImagePickerDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Choose from Library", color = Color.White)
                    }
                }

            }
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
                .fillMaxWidth(0.97f)
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
                        supportingText = { viewModel.typeError?.let { Text(it, color = Color.Red) } },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
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
                        value = selectedCategory?.let { "${it.icon} ${it.title}" } ?: "Select Category",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        isError = viewModel.categoryError != null,
                        supportingText = { viewModel.categoryError?.let { Text(it, color = Color.Red) } },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
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
                    value = viewModel.inputLocation,
                    onValueChange = {
                        viewModel.inputLocation = it
                        viewModel.validateInputs()
                    },
                    label = { Text("Location") },
                    isError = viewModel.locationError != null,
                    supportingText = { viewModel.locationError?.let { Text(it, color = Color.Red) } },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                hasUsedFetchedLocation = false
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

                // Upload button (optional)
                if (user != null) {
                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.CAMERA
                                )
                                != PackageManager.PERMISSION_GRANTED
                            ) {
                                permissionLauncher.launch(android.Manifest.permission.CAMERA)
                            } else {
                                showImagePickerDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Upload photo/receipt", color = White)
                    }
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
                                    name = viewModel.inputName,
                                    type = viewModel.inputType.lowercase(),
                                    amount = viewModel.inputAmount.toDouble(),
                                    categoryId = viewModel.inputCategoryId!!,
                                    date = viewModel.inputDate ?: System.currentTimeMillis(),
                                    note = viewModel.inputNote,
                                    location = viewModel.inputLocation,
                                    imageUrl = viewModel.inputImagePath,
                                )
                                onSave(transaction)
                                viewModel.resetInputFields()
                                onDismiss()
                            }
                        },
                        enabled = viewModel.validateInputs(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonBlue),
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

fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String? {
    return try {
        val filename = "receipt_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, filename)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        file.absolutePath // return file path
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}