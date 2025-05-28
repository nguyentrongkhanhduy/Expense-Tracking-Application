package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.datastore.UserPreferences
import com.example.myapplication.ui.theme.PrimaryBlue
import com.example.myapplication.ui.theme.White
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        delay(1500L)

        val isFirst = UserPreferences.isFirstLaunch(context)
        val isGuest = UserPreferences.isGuestMode(context)

        if (isFirst) {
            navController.navigate("welcome") {
                popUpTo("splash") { inclusive = true }
            }
            UserPreferences.setFirstLaunch(context, false)
        } else {
            navController.navigate("home?isGuest=${isGuest ?: false}") {
                popUpTo("splash") { inclusive = true }
            }
        }

    }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(PrimaryBlue),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("My Spend", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = White)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Tracking cents so you don't make none!", fontSize = 18.sp, color = White, fontStyle = FontStyle.Italic)

        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    val navController = rememberNavController()
    SplashScreen(navController)
}