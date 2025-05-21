package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.screens.HomeScreen
import com.example.myapplication.screens.LoginScreen
import com.example.myapplication.screens.SignUpScreen
import com.example.myapplication.screens.WelcomeScreen
import com.example.myapplication.viewmodel.AuthViewModel


@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val viewModel = AuthViewModel()
    NavHost(navController, startDestination = "welcome") {
        composable("welcome") { WelcomeScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignUpScreen(navController, viewModel) }
        composable("home") { HomeScreen() }
    }
}
