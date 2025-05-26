package com.example.myapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.viewmodel.AuthViewModel

@Composable
fun HomeScreen(navController: NavController, viewModel: AuthViewModel) {
    val user by viewModel.user.collectAsState()
    val isSignedIn by viewModel.isSignedIn.collectAsState()

    LaunchedEffect(isSignedIn) {
        if (!isSignedIn) {
            navController.navigate("login?showGuest=true") {
                popUpTo(0)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            user?.let {
                Text(
                    text = it.displayName,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { viewModel.signOut() },
            ) {
                Text("Log out")
            }
        }

    }
}

