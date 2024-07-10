package com.example.flightapp

import AccountSettingsScreen
import android.annotation.SuppressLint
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.flightapp.viewmodel.LoginViewModel

/**
 * MainNavigation function sets up the main navigation structure of the app.
 * It includes a bottom navigation bar and handles navigation between different screens.
 * Uses NavHost and NavController for navigation instead of explicit Intents.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainNavigation() {
    val navController = rememberNavController() // Initialize NavController to manage app navigation
    val loginViewModel: LoginViewModel = viewModel()
    // Scaffold layout with a bottom navigation bar defining all the tabs
    Scaffold(
        bottomBar = {
            BottomNavigation (
                backgroundColor = MaterialTheme.colors.background
            ){
                val navBackStackEntry by navController.currentBackStackEntryAsState()  // Observe navigation back stack
                val currentDestination = navBackStackEntry?.destination // Get the current destination
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentDestination?.route == "main", // Navigate to the "main" route
                    onClick = {
                        navController.navigate("main") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
                // Search navigation item
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    label = { Text("Search") },
                    selected = currentDestination?.route == "search",
                    onClick = {
                        navController.navigate("search") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
                // My Trips navigation item
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Info, contentDescription = "My Trips") },
                    label = { Text("My Trips") },
                    selected = currentDestination?.route == "myTrips",
                    onClick = {
                        navController.navigate("myTrips") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
                // My Trips navigation item
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = currentDestination?.route == "settings",
                    onClick = {
                        navController.navigate("settings") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) {
        // Call to the NavigationGraph function to set up navigation between screens
        NavigationGraph(navController = navController, loginViewModel=loginViewModel)
    }
}

/**
 * NavigationGraph function sets up the navigation routes for the app.
 * It defines the composable functions for each screen and handles navigation between them.
 * Uses NavHost and NavController for managing navigation and state.
 */
@Composable
fun NavigationGraph(navController: NavHostController,loginViewModel: LoginViewModel) {
    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen() }
        composable("search") { SearchScreen(navController = navController) }
        composable("myTrips") { FlightDetailsScreen() }
        composable("settings") {
            AccountSettingsScreen(loginViewModel = loginViewModel, navController = navController)
        }
    }
}
