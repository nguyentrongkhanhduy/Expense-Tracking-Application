package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.data.model.Category
import com.example.myapplication.screens.dialogs.AddCategoryDialog
import com.example.myapplication.screens.dialogs.EditCategoryDialog
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.ui.theme.PrimaryGreen
import com.example.myapplication.ui.theme.PrimaryRed
import com.example.myapplication.ui.theme.White
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.category.CategoryViewModel

@Composable
fun CategoriesScreen(
    navController: NavController,
    viewModel: CategoryViewModel,
    authViewModel: AuthViewModel
) {
    var filter by remember { mutableStateOf("All") }
    val categories by viewModel.categories.collectAsState()
    val filterOptions = listOf("All", "Expense", "Income")
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }

    val user by authViewModel.user.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .padding(horizontal = 18.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
                .height(56.dp)
        ) {
            IconButton(
                onClick = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("selectedTab", 3)
                    navController.popBackStack()
                },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = PrimaryBlue
                )
            }
            Text(
                "My Categories",
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = Color(0xFF0A2540),
                modifier = Modifier.align(Alignment.Center)
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterEnd)
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        val currentFilterIndex = filterOptions.indexOf(filter)
        val nextFilter = filterOptions[(currentFilterIndex + 1) % filterOptions.size]
        val buttonColor = when (filter) {
            "Expense" -> PrimaryRed
            "Income" -> PrimaryGreen
            else -> PrimaryBlue
        }

        Button(
            onClick = {
                filter = nextFilter
            },
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(160.dp)
                .height(38.dp)
        ) {
            Text(filter, color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(18.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            items(
                categories.filter {
                    when (filter) {
                        "All" -> true
                        "Expense" -> it.type.equals("expense", ignoreCase = true)
                        "Income" -> it.type.equals("income", ignoreCase = true)
                        else -> true
                    }
                }
            ) { category ->
                CategoryCard(category) { editingCategory = category }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            FloatingActionButton(
                onClick = {
                    viewModel.resetInputFields()
                    showAddDialog = true
                },
                containerColor = Color(0xFF22304B)
            ) {
                Text("+", color = Color.White, fontSize = 32.sp)
            }
        }
    }

    if (showAddDialog) {
        AddCategoryDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
            onSave = { type, title, icon, limit ->
                val newCat = Category(
                    categoryId = System.currentTimeMillis(),
                    type = type,
                    title = title,
                    icon = icon,
                    limit = limit?.toDoubleOrNull(),
                    updatedAt = System.currentTimeMillis()
                )

                viewModel.addCategory(newCat)

                if (user != null) {
                    viewModel.addCategoryToFirestore(newCat, user!!.uid)
                }
            }
        )
    }

    if (editingCategory != null) {
        EditCategoryDialog(
            initialCategory = editingCategory!!,
            onDismiss = { editingCategory = null },
            onDelete = {
                viewModel.deleteCategory(editingCategory!!)

                if (user != null) {
                    viewModel.deleteCategoryFromFirestore(editingCategory!!.categoryId, user!!.uid)
                }

                editingCategory = null
            },
            onSave = { type, title, icon, limit ->
                val updatedCat = editingCategory!!.copy(
                    type = type,
                    title = title,
                    icon = icon,
                    limit = limit?.toDoubleOrNull(),
                    updatedAt = System.currentTimeMillis()
                )
                viewModel.updateCategory(updatedCat)

                if (user != null) {
                    viewModel.updateCategoryInFirestore(updatedCat, user!!.uid)
                }

                editingCategory = null
            }
        )
    }
}


@Composable
fun CategoryCard(category: Category, onClick: () -> Unit) {
    val bgColor = when (category.type.lowercase()) {
        "expense" -> Color(0xFFFFB3B3)
        "income" -> Color(0xFF7ED957)
        else -> Color(0xFFE0E0E0)
    }
    val iconShape: Shape = when (category.icon) {
        "◆" -> RoundedCornerShape(6.dp)
        "▲" -> RoundedCornerShape(0.dp)
        "■" -> RoundedCornerShape(2.dp)
        "★" -> CircleShape
        "●" -> CircleShape
        else -> CircleShape
    }
    val iconBg = when (category.icon) {
        "◆" -> Color.Yellow
        "▲" -> Color.Red
        "■" -> Color.Blue
        "★" -> Color(0xFFFFA500)
        "●" -> Color(0xFF5D5FEF)
        else -> Color.White
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(bgColor, RoundedCornerShape(22.dp))
            .padding(horizontal = 18.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(iconShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.icon,
                fontSize = 22.sp,
                color = Color.Black
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = category.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF22304B),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$${category.limit?.let { "%.2f".format(it) } ?: "0.00"}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .background(
                    if (category.type.lowercase() == "expense") Color(0xFFFF6B6B)
                    else Color(0xFF4CAF50),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}