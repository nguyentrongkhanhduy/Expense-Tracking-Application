package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.screens.CategoriesScreen
import com.example.myapplication.screens.HomeScreen
import com.example.myapplication.screens.LoginScreen
import com.example.myapplication.screens.SignUpScreen
import com.example.myapplication.screens.SplashScreen
import com.example.myapplication.screens.WelcomeScreen
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.CategoryViewModel
import com.example.myapplication.viewmodel.CategoryViewModelFactory
import com.example.myapplication.viewmodel.TransactionViewModel
import com.example.myapplication.viewmodel.TransactionViewModelFactory

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current
    val categoryViewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory(context)
    )
    val transactionViewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModelFactory(context)
    )

    NavHost(navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("welcome") { WelcomeScreen(navController) }
        composable(
            route = "login?showGuest={showGuest}",
            arguments = listOf(navArgument("showGuest") { defaultValue = "false" })
        ) { backStackEntry ->
            val showGuest = backStackEntry.arguments?.getString("showGuest").toBoolean()
            LoginScreen(
                navController = navController,
                viewModel = authViewModel,
                showGuestOption = showGuest
            )
        }
        composable("signup") { SignUpScreen(navController, authViewModel) }
        composable(
            route = "home?isGuest={isGuest}",
            arguments = listOf(navArgument("isGuest") { defaultValue = "false" })
        ) { backStackEntry ->
            val isGuest = backStackEntry.arguments?.getString("isGuest").toBoolean()
            HomeScreen(
                navController = navController,
                authViewModel = authViewModel,
                transactionViewModel = transactionViewModel,
                categoryViewModel = categoryViewModel,
                isGuest = isGuest // Uncomment if you use this param
            )
        }
        composable("categories") {
            CategoriesScreen(
                navController = navController,
                viewModel = categoryViewModel
            )
        }
    }
}
