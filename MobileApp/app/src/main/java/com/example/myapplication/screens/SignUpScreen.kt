package com.example.myapplication.screens

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.components.BackButton
import com.example.myapplication.components.MyButton
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.category.CategoryViewModel

@Composable
fun SignUpScreen(navController: NavController, viewModel: AuthViewModel, categoryViewModel: CategoryViewModel) {
    var email by remember { mutableStateOf("test@123.com") }
    var password by remember { mutableStateOf("1234567") }
    var confirmPassword by remember { mutableStateOf("1234567") }
    var displayName by remember { mutableStateOf("test123") }

    val isSignedUp by viewModel.isSignedUp.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(isSignedUp) {
        if (isSignedUp) {
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
            modifier = Modifier.align(Alignment.TopStart),
            onClick = { navController.popBackStack() }
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp),
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
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            MyButton(
                onClick = { viewModel.signUp(email, password, displayName) {
                    if (categoryViewModel.categories.value.isEmpty()) {
                        categoryViewModel.initializeDefaults(it)
                    } else {
                        categoryViewModel.initializeDefaultsForFirestore(it)
                    }
                } },
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && password.length >= 6 && confirmPassword.isNotBlank() && displayName.isNotBlank() && password == confirmPassword,
            ) {
                if (isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(
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
//
//
//@Preview(showBackground = true)
//@Composable
//fun SignUpScreenPreview() {
//    val navController = rememberNavController()
//    SignUpScreen(navController = navController, viewModel = AuthViewModel())
//}