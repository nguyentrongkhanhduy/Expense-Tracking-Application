package com.example.myapplication.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.components.BackButton
import com.example.myapplication.components.MyButton
import com.example.myapplication.screens.dialogs.StyledAlertDialog
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.category.CategoryViewModel
import com.example.myapplication.viewmodel.transaction.TransactionViewModel
import com.google.firebase.messaging.FirebaseMessaging

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
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    // Tooltip states
    var showEmailTooltip by remember { mutableStateOf(false) }
    var showPasswordTooltip by remember { mutableStateOf(false) }
    var showConfirmTooltip by remember { mutableStateOf(false) }
    var showDisplayNameTooltip by remember { mutableStateOf(false) }

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
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sign Up",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            Spacer(Modifier.height(32.dp))

            // Email field
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
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
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    IconButton(
                        onClick = { showEmailTooltip = !showEmailTooltip },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.help),
                            contentDescription = "Help icon",
                            tint = PrimaryBlue
                        )
                    }
                }
                if (showEmailTooltip) {
                    Text(
                        text = "Enter a valid email address (e.g., user@example.com).",
                        fontSize = 11.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(Color(0xFFEEF4FF), shape = RoundedCornerShape(8.dp))
                            .border(1.dp, PrimaryBlue, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .widthIn(max = 260.dp)
                    )
                }
            }

            //Password field
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
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
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    IconButton(
                        onClick = { showPasswordTooltip = !showPasswordTooltip },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.help),
                            contentDescription = "Help icon",
                            tint = PrimaryBlue
                        )
                    }
                }
                if (showPasswordTooltip) {
                    Text(
                        text = "Password with 6+ chars, letters, numbers & symbols.",
                        fontSize = 11.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(Color(0xFFEEF4FF), shape = RoundedCornerShape(8.dp))
                            .border(1.dp, PrimaryBlue, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .widthIn(max = 260.dp)
                    )
                }

            }

            // Confirm Password field
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
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
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    IconButton(
                        onClick = { showConfirmTooltip = !showConfirmTooltip },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.help),
                            contentDescription = "Help icon",
                            tint = PrimaryBlue
                        )
                    }
                }
                if (showConfirmTooltip) {
                    Text(
                        text = "Re-enter your password to confirm",
                        fontSize = 11.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(Color(0xFFEEF4FF), shape = RoundedCornerShape(8.dp))
                            .border(1.dp, PrimaryBlue, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .widthIn(max = 260.dp)
                    )
                }
            }

            // Display Name field
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
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
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    IconButton(
                        onClick = { showDisplayNameTooltip = !showDisplayNameTooltip },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.help),
                            contentDescription = "Help icon",
                            tint = PrimaryBlue
                        )
                    }
                }
                if (showDisplayNameTooltip) {
                    Text(
                        text = "Choose the name you'd like displayed in the app.",
                        fontSize = 11.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(Color(0xFFEEF4FF), shape = RoundedCornerShape(8.dp))
                            .border(1.dp, PrimaryBlue, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .widthIn(max = 260.dp)
                    )
                }
            }

            // Submit button
            MyButton(
                onClick = {
                    val trimmedEmail = email.trim()
                    val trimmedPassword = password.trim()
                    val trimmedConfirmPassword = confirmPassword.trim()
                    val trimmedDisplayName = displayName.trim()

                    emailError = if (!AuthViewModel.isValidEmail(trimmedEmail)) "Invalid email address" else null
                    passwordError = if (!AuthViewModel.isValidPassword(trimmedPassword)) "Password must have be 6 characters long with uppercase/lowercase letters, numbers, and special symbols" else null
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
                            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val token = task.result
                                    viewModel.sendFCMTokenToServer(it, token)
                                }
                            }
                        }
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(4.dp).size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Create Account", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
