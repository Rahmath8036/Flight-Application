package com.example.flightapp

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flightapp.data.AppDatabase
import com.example.flightapp.data.BookedFlightDao
import com.example.flightapp.data.FlightDao
import com.example.flightapp.data.FlightRepository
import com.example.flightapp.viewmodel.LoginViewModel
import com.example.flightapp.viewmodel.NetworkStatusChecker
import com.example.flightapp.viewmodel.SignUpViewModel
import com.google.firebase.Firebase
import com.google.firebase.initialize

/**
 * MainActivity is the entry point for the app, handling initialization and setting up the main content.
 */
class MainActivity : ComponentActivity() {
    private lateinit var flightDao: FlightDao
    private lateinit var bookedFlightDao: BookedFlightDao
    private lateinit var repository: FlightRepository
    private lateinit var networkStatusChecker: NetworkStatusChecker
    /**
     * onCreate method is called when the activity is first created.
     * It initializes Firebase, sets up the database, and starts syncing my room database flights with Firebase for my content provider.
     */
    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(this) // Initialize Firebase

        val appDatabase = AppDatabase.getInstance(this)
        flightDao = appDatabase.flightDao() // Initialize Flight DAO
        bookedFlightDao = appDatabase.bookedFlightDao() // Initialize BookedFlight DAO
        repository = FlightRepository(flightDao, bookedFlightDao) // Initialize the repository with DAOs

        repository.syncFlightsWithFirebase() // Start syncing flights with Firebase

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkStatusChecker = NetworkStatusChecker(connectivityManager)
        createNotificationChannel() // Create a notification channel for the app
        setContent {
            MyApp(networkStatusChecker) // Set the main content of the app
        }
    }
    /**
     * Lifecycle method called when the activity becomes visible to the user.
     */
    override fun onStart() {
        super.onStart()
        Log.v("Activity Life Cycle Methods","onStart")
    }
    /**
     * Lifecycle method called when the activity starts interacting with the user.
     */
    override fun onResume() {
        super.onResume()
        Log.v("Activity Life Cycle Methods","onResume")
    }
    /**
     * Lifecycle method called when the system is about to start resuming another activity.
     */
    override fun onPause() {
        super.onPause()
        Log.v("Activity Life Cycle Methods","onPause")
    }
    /**
     * Lifecycle method called when the activity is no longer visible to the user.
     */
    override fun onStop() {
        super.onStop()
        Log.v("Activity Life Cycle Methods","onStop")
    }
    /**
     * Lifecycle method called before the activity is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.v("Activity Life Cycle Methods","onDestroy")
    }
    /**
     * Composable function that sets up the main UI of the app.
     * Handles login state and displays the appropriate screen.
     */
    @Composable
    fun MyApp(networkStatusChecker: NetworkStatusChecker) {
        val loginViewModel: LoginViewModel = viewModel()
        val signUpViewModel: SignUpViewModel = viewModel()
        val isLoggedIn by loginViewModel.isLoggedIn.collectAsState()
        var showSignUp by rememberSaveable { mutableStateOf(false) }

        if (isLoggedIn) {
            val userId = loginViewModel.userId  // Make sure this value is populated in your ViewModel when the user logs in
            MainNavigation() // Main app content
            repository.fetchBookedFlightsForUser(userId)
        } else {
            if (showSignUp) {
                SignUpScreen(
                    viewModel = signUpViewModel,
                    onSignUpSuccess = {
                        // Optionally reset showSignUp if you log in the user directly after signup
                        showSignUp = false
                    },
                    onNavigateBackToLogin = {
                        showSignUp = false
                    }
                )
            } else {
                LoginScreen(
                    viewModel = loginViewModel,
                    networkStatusChecker,
                    onLoginSuccess = {
                        // Handle what happens on login success, if additional actions are needed
                    },
                    onNavigateToSignUp = {
                        showSignUp = true  // Toggle to show sign up screen
                    }
                )
            }
        }
    }
    /**
     * Creates a notification channel for the app.
     * Required for displaying notifications on Android Oreo and above.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name) // Define the name of the channel
            val descriptionText = getString(R.string.channel_description) // Description of the channel
            val importance = NotificationManager.IMPORTANCE_HIGH // Set the importance level for the channel
            val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
                description = descriptionText // Set the description of the channel
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager // Get the NotificationManager service
            notificationManager.createNotificationChannel(channel) // Create the notification channel
        }
    }
}
