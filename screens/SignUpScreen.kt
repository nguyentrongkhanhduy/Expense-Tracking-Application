package com.example.myapplication.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun SignUpScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
    ) {
        Text(
            "< Back",
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp)
                .clickable { navController.popBackStack() }
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sign Up",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = "",
                onValueChange = { /* TODO: Handle email input */ },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = "",
                onValueChange = { /* TODO: Handle password input */ },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = "",
                onValueChange = { /* TODO: Handle confirm password input */ },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { /* TODO: Handle sign up action */ },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Account")
            }
        }
    }
}
