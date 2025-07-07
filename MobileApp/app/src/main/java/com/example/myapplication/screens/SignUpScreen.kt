package com.example.myapplication.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.components.BackButton
import com.example.myapplication.components.MyButton
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.category.CategoryViewModel
import com.example.myapplication.viewmodel.transaction.TransactionViewModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import com.example.myapplication.screens.dialogs.StyledAlertDialog

@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: AuthViewModel,
    categoryViewModel: CategoryViewModel,
    transactionViewModel: TransactionViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var displayNameError by remember { mutableStateOf<String?>(null) }

    val isSignedUp by viewModel.isSignedUp.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val context = LocalContext.current
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(isSignedUp) {
        if (isSignedUp) {
            Toast.makeText(context, "Account successfully created", Toast.LENGTH_LONG).show()
            navController.navigate("home") {
                popUpTo("signup") { inclusive = true }
            }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        BackButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.statusBars),
            onClick = { navController.popBackStack() }
        )
        if (errorMessage != null) {
            StyledAlertDialog(
                title = "Sign Up Error",
                message = errorMessage ?: "",
                confirmButtonText = "OK",
                onConfirm = { viewModel.clearError() }
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
            .verticalScroll(rememberScrollState()) // Make scrollable
            .imePadding(),                         // Handle keyboard insets
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sign Up",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            Spacer(Modifier.height(32.dp))
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError != null) emailError = null
                },
                label = { Text("Email") },
                isError = emailError != null,
                supportingText = { emailError?.let { Text(it) } },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (passwordError != null) passwordError = null
                },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError != null,
                supportingText = { passwordError?.let { Text(it) } },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (confirmPasswordError != null) confirmPasswordError = null
                },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                isError = confirmPasswordError != null,
                supportingText = { confirmPasswordError?.let { Text(it) } },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = displayName,
                onValueChange = {
                    displayName = it
                    if (displayNameError != null) displayNameError = null
                },
                label = { Text("Display Name") },
                isError = displayNameError != null,
                supportingText = { displayNameError?.let { Text(it) } },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            MyButton(
                onClick = {
                    val trimmedEmail = email.trim()
                    val trimmedPassword = password.trim()
                    val trimmedConfirmPassword = confirmPassword.trim()
                    val trimmedDisplayName = displayName.trim()

                    emailError = if (!AuthViewModel.isValidEmail(trimmedEmail)) "Invalid email address" else null
                    passwordError = if (!AuthViewModel.isValidPassword(trimmedPassword)) "Password must be at least 6 characters" else null
                    confirmPasswordError = if (trimmedConfirmPassword != trimmedPassword) "Passwords do not match" else null
                    displayNameError = if (trimmedDisplayName.isBlank()) "Display name cannot be empty" else null

                    if (emailError == null && passwordError == null && confirmPasswordError == null && displayNameError == null) {
                        viewModel.signUp(trimmedEmail, trimmedPassword, trimmedDisplayName) {
                            if (categoryViewModel.categories.value.isEmpty()) {
                                categoryViewModel.initializeDefaults(it)
                            } else {
                                categoryViewModel.syncDataWhenSignUp(it)
                                transactionViewModel.syncDataWhenSignUp(it)
                            }
                        }
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Create Account", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
