package com.example.myapplication.screens.dialogs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.data.model.Category
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.ui.theme.PrimaryRed
import com.example.myapplication.ui.theme.White
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoryDialog(
    initialCategory: Category,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onSave: (String, String, String, String?) -> Unit
) {
    val context = LocalContext.current
    val options = listOf("expense", "income")
    var expanded by remember { mutableStateOf(false) }
    val isEnable = initialCategory.categoryId > 0L

    var type by rememberSaveable(initialCategory.categoryId) { mutableStateOf(initialCategory.type) }
    var title by rememberSaveable(initialCategory.categoryId) { mutableStateOf(initialCategory.title) }
    var icon by rememberSaveable(initialCategory.categoryId) { mutableStateOf(initialCategory.icon) }
    var limit by rememberSaveable(initialCategory.categoryId) {
        mutableStateOf(initialCategory.limit?.toString() ?: "")
    }

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box {
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
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = PrimaryBlue
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Edit Category",
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.width(40.dp))
                    }

                    // Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                    ) {
                        OutlinedTextField(
                            value = type,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
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

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = icon,
                            onValueChange = { icon = it },
                            label = { Text("Icon (emoji or symbol)") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    if (type == "expense") {
                        OutlinedTextField(
                            value = limit,
                            onValueChange = { limit = it },
                            label = { Text("Limit") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }


                    if (isEnable) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = {
                                    showDeleteConfirmation = true
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
                                    onSave(
                                        type,
                                        title,
                                        icon,
                                        limit.takeIf { it.isNotBlank() }
                                    )
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(20.dp),
                                enabled = type.isNotBlank() && title.isNotBlank() && icon.isNotBlank(),
                                elevation = ButtonDefaults.buttonElevation(4.dp)
                            ) {
                                Text("Save", color = White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Confirmation dialog overlay
            if (showDeleteConfirmation) {
                ConfirmationDialog(
                    title = "Delete Category",
                    message = "Are you sure you want to delete this category? This action cannot be undone.",
                    onConfirm = {
                        showDeleteConfirmation = false
                        onDelete()
                        Toast.makeText(context, "Category deleted", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    onCancel = { showDeleteConfirmation = false },
                    onDismiss = { showDeleteConfirmation = false }
                )
            }
        }
    }
}
