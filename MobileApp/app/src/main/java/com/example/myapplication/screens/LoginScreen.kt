package com.example.myapplication.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.components.BackButton
import com.example.myapplication.components.MyButton
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.viewmodel.AuthViewModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.screens.dialogs.StyledAlertDialog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.myapplication.R

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel,
    showGuestOption: Boolean = false
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val isSignedIn by viewModel.isSignedIn.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(isSignedIn) {
        if (isSignedIn) {
            Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        if (navController.previousBackStackEntry != null) {
            BackButton(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .windowInsetsPadding(WindowInsets.statusBars),
                onClick = { navController.popBackStack() }
            )
        }

        if (errorMessage != null) {
            StyledAlertDialog(
                title = "Login Error",
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
                text = "Login",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            Spacer(Modifier.height(32.dp))

            // Email field with tooltip below input row
            var showEmailTooltip by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
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
                        fontSize = 13.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(Color(0xFFEEF4FF), shape = RoundedCornerShape(8.dp))
                            .border(1.dp, PrimaryBlue, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .widthIn(max = 260.dp)
                    )
                }
            }

            // Password field with tooltip below input row
            var showPasswordTooltip by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
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
                        text = "Enter your account password. It’s case sensitive.",
                        fontSize = 13.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(Color(0xFFEEF4FF), shape = RoundedCornerShape(8.dp))
                            .border(1.dp, PrimaryBlue, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .widthIn(max = 260.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            MyButton(
                onClick = {
                    val trimmedEmail = email.trim()
                    val trimmedPassword = password.trim()

                    emailError = if (!AuthViewModel.isValidEmail(trimmedEmail)) "Invalid email address" else null
                    passwordError = if (trimmedPassword.isBlank()) "Password cannot be empty" else null

                    if (emailError == null && passwordError == null) {
                        viewModel.signIn(trimmedEmail, trimmedPassword) {
                            viewModel.setSyncPrompt(true)
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
                    Text("Login", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account?", fontSize = 16.sp)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Sign Up",
                    color = PrimaryBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { navController.navigate("signup") }
                )
            }

            Spacer(Modifier.height(32.dp))

            if (showGuestOption) {
                Text("Or", color = PrimaryBlue, fontSize = 18.sp)
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = {
                    navController.navigate("home?isGuest=true&selectedTab=0") {
                        popUpTo("login") { inclusive = true }
                    }
                }) {
                    Text("Continue as guest", color = PrimaryBlue, fontSize = 20.sp)
                }
            }
        }
    }
}