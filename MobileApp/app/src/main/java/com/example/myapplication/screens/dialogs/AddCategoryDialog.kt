package com.example.myapplication.screens.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.ui.theme.PrimaryRed
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.theme.InputBlue
import com.example.myapplication.viewmodel.category.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryDialog(
    viewModel: CategoryViewModel,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String?) -> Unit
) {
    val options = listOf("expense", "income")
    var expanded by remember { mutableStateOf(false) }

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
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryBlue
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "New Category",
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
                        value = viewModel.inputType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        isError = viewModel.typeError != null,
                        supportingText = { viewModel.typeError?.let { Text(it, color = Color.Red, fontSize = 12.sp) } },
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
                                    viewModel.inputType = selectionOption
                                    viewModel.validateInputs()
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = viewModel.inputTitle,
                    onValueChange = {
                        viewModel.inputTitle = it
                        viewModel.validateInputs()
                    },
                    label = { Text("Title") },
                    isError = viewModel.titleError != null,
                    supportingText = { viewModel.titleError?.let { Text(it, color = Color.Red, fontSize = 12.sp) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // Icon with plus button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = viewModel.inputIcon,
                        onValueChange = {
                            viewModel.inputIcon = it
                            viewModel.validateInputs()
                        },
                        label = { Text("Generate Icon") },
                        isError = viewModel.iconError != null,
                        supportingText = { viewModel.iconError?.let { Text(it, color = Color.Red, fontSize = 12.sp) } },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = { /* Generate icon logic here */ },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(InputBlue)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Generate Icon", tint = White)
                    }
                }

                OutlinedTextField(
                    value = viewModel.inputLimit,
                    onValueChange = {
                        viewModel.inputLimit = it
                        viewModel.validateInputs()
                    },
                    label = { Text("Limit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = viewModel.limitError != null,
                    supportingText = { viewModel.limitError?.let { Text(it, color = Color.Red, fontSize = 12.sp) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(4.dp)
                    ) {
                        Text("Cancel", color = White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            if (viewModel.validateInputs()) {
                                onSave(
                                    viewModel.inputType,
                                    viewModel.inputTitle,
                                    viewModel.inputIcon,
                                    viewModel.inputLimit.takeIf { it.isNotBlank() }
                                )
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        enabled = viewModel.inputType.isNotBlank() &&
                                viewModel.inputTitle.isNotBlank() &&
                                viewModel.inputIcon.isNotBlank() &&
                                viewModel.typeError == null &&
                                viewModel.titleError == null &&
                                viewModel.iconError == null &&
                                viewModel.limitError == null,
                        elevation = ButtonDefaults.buttonElevation(4.dp)
                    ) {
                        Text("Save", color = White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
