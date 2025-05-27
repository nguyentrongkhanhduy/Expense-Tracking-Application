package com.example.myapplication.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.components.MyButton
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.ui.theme.White

@Composable
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to My Spend",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = PrimaryBlue
        )
        Spacer(Modifier.height(32.dp))
        TextButton(onClick = { navController.navigate("home?isGuest=true") }) {
            Text("Continue as guest", color = PrimaryBlue, fontSize = 20.sp)
        }
        Spacer(Modifier.height(16.dp))
        Text("Or", color = PrimaryBlue, fontSize = 18.sp)
        Spacer(Modifier.height(24.dp))
        MyButton(onClick = { navController.navigate("login") }) {
            Text("Login", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    val navController = rememberNavController()
    WelcomeScreen(navController)
}
