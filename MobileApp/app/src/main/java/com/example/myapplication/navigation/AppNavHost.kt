package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.screens.HomeScreen
import com.example.myapplication.screens.LoginScreen
import com.example.myapplication.screens.SignUpScreen
import com.example.myapplication.screens.SplashScreen
import com.example.myapplication.screens.WelcomeScreen
import com.example.myapplication.viewmodel.AuthViewModel


@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val viewModel: AuthViewModel = viewModel()
    NavHost(navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("welcome") { WelcomeScreen(navController) }
        composable(
            route = "login?showGuest={showGuest}",
            arguments = listOf(
                navArgument("showGuest") {
                    defaultValue = "false"
                }
            )
        ) { backStackEntry ->
            val showGuest = backStackEntry.arguments?.getString("showGuest").toBoolean()

            LoginScreen(
                navController = navController,
                viewModel = viewModel,
                showGuestOption = showGuest
            )
        }
        composable("signup") { SignUpScreen(navController, viewModel) }
        composable(
            route = "home?isGuest={isGuest}",
            arguments = listOf(
                navArgument("isGuest") {
                    defaultValue = "false"
                }
            )
        ) {backStackEntry ->
            val isGuest = backStackEntry.arguments?.getString("isGuest").toBoolean()
            HomeScreen(
                navController =navController,
                viewModel = viewModel,
                isGuest = isGuest
            )
        }
    }
}
