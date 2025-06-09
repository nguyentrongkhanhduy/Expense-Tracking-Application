package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
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
import com.example.myapplication.viewmodel.category.CategoryViewModel
import com.example.myapplication.viewmodel.category.CategoryViewModelFactory
import com.example.myapplication.viewmodel.CurrencyViewModel
import com.example.myapplication.viewmodel.LocationViewModel
import com.example.myapplication.viewmodel.transaction.TransactionViewModel
import com.example.myapplication.viewmodel.transaction.TransactionViewModelFactory

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val locationViewModel: LocationViewModel = viewModel()
    val context = LocalContext.current
    val categoryViewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory(context)
    )
    val transactionViewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModelFactory(context)
    )
    val currencyViewModel: CurrencyViewModel = viewModel()

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
        composable("signup") { SignUpScreen(navController, authViewModel, categoryViewModel) }
        composable(
            route = "home?isGuest={isGuest}&selectedTab={selectedTab}",
            arguments = listOf(
                navArgument("isGuest") { defaultValue = "false" },
                navArgument("selectedTab") {
                    type = NavType.IntType
                    defaultValue = 0 }
            )
        ) { backStackEntry ->
            val isGuest = backStackEntry.arguments?.getString("isGuest").toBoolean()
            val selectedTab = backStackEntry.arguments?.getString("selectedTab")?.toIntOrNull() ?: 0
            HomeScreen(
                navController = navController,
                authViewModel = authViewModel,
                transactionViewModel = transactionViewModel,
                categoryViewModel = categoryViewModel,
                locationViewModel = locationViewModel,
                currencyViewModel = currencyViewModel,
                isGuest = isGuest,
                initialTab = selectedTab
            )
        }

        composable(
            route = "categories?fromTab={fromTab}",
            arguments = listOf(navArgument("fromTab") { defaultValue = "0" })
        ) { backStackEntry ->
            CategoriesScreen(
                navController = navController,
                viewModel = categoryViewModel,
            )
        }

    }
}
