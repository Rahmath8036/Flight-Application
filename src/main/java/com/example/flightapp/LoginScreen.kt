package com.example.flightapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.flightapp.viewmodel.LoginViewModel
import com.example.flightapp.viewmodel.NetworkStatusChecker

/**
 * The main login screen of the app, which contains the UI for login and navigation to the sign-up screen.
 *
 * @param viewModel The LoginViewModel to handle login logic.
 * @param networkStatusChecker The NetworkStatusChecker to check network connectivity.
 * @param onLoginSuccess Callback function to execute on successful login.
 * @param onNavigateToSignUp Callback function to navigate to the sign-up screen.
 */
@Composable
fun LoginScreen(viewModel: LoginViewModel, networkStatusChecker: NetworkStatusChecker, onLoginSuccess: () -> Unit, onNavigateToSignUp: () -> Unit) {
    val loginState by viewModel.loginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val email = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }
    val showNoWifiSnackbar = remember { mutableStateOf(false) }

    // Handle login state changes
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginViewModel.LoginState.Success -> onLoginSuccess()
            is LoginViewModel.LoginState.Error -> snackbarHostState.showSnackbar((loginState as LoginViewModel.LoginState.Error).message)
            else -> {}
        }
    }
    // Handle no WiFi snackbar display
    LaunchedEffect(showNoWifiSnackbar.value) {
        if (showNoWifiSnackbar.value) {
            snackbarHostState.showSnackbar("No WiFi connection detected. Please connect to WiFi.")
            showNoWifiSnackbar.value = false
        }
    }

    // Layout of the login screen
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1565C0)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // SkySailor Logo
        item {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(200.dp)
            )
        }
        item{Spacer(modifier = Modifier.height(16.dp))}
        // Welcome message
        item{
            Text(
                "Welcome to SkySailor",
                style = MaterialTheme.typography.headlineMedium.copy(color = Color.White)
            )
        }
        item{
            Spacer(
                modifier = Modifier.height(32.dp)
            )
        }
        // Text fields for email and password entry
        item{
            TextFieldSection(email, password)
        }
        // Login Button
        item{
            LoginButton(email, password, viewModel,networkStatusChecker, showNoWifiSnackbar)
        }
        item{
            Spacer(modifier = Modifier.height(20.dp))
        }
        // Sign Up Button
        item{
            Button(
                onClick = onNavigateToSignUp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1565C0)
                )
        ) {
            Text("Sign Up", modifier = Modifier.padding(8.dp))
        }}
    }
    SnackbarHost(hostState = snackbarHostState)
}

/**
 * Composable function to display the email and password input fields.
 *
 * @param email MutableState to hold the email input value.
 * @param password MutableState to hold the password input value.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldSection(email: MutableState<String>, password: MutableState<String>) {
    // Define colors for the text fields
    val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        focusedBorderColor = Color.White,
        unfocusedBorderColor = Color.White,
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.White
    )

    // Email Input Field
    OutlinedTextField(
        value = email.value,
        onValueChange = { email.value = it },
        label = { Text("Email") },
        singleLine = true,
        textStyle = TextStyle(color = Color.White),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
        colors = textFieldColors,
        placeholder = {
            Text("Enter your email", style = TextStyle(color = Color.Gray))  // Placeholder text and color
        }
    )
    Spacer(modifier = Modifier.height(16.dp))
    // Password Input Field
    OutlinedTextField(
        value = password.value,
        onValueChange = { password.value = it },
        label = { Text("Password") },
        singleLine = true,
        textStyle = TextStyle(color = Color.White),  // Set the input text color here
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        colors = textFieldColors,
        placeholder = {
            Text("Enter your password", style = TextStyle(color = Color.Gray))  // Placeholder text and color
        }
    )
    Spacer(modifier = Modifier.height(24.dp))
}

/**
 * Composable function to display the login button and handle the login functionality.
 *
 * @param email MutableState to hold the email input value.
 * @param password MutableState to hold the password input value.
 * @param viewModel The LoginViewModel to handle login logic.
 * @param networkStatusChecker The NetworkStatusChecker to check network connectivity.
 * @param showNoWifiSnackbar MutableState to trigger the snackbar for no WiFi connection.
 */
@Composable
fun LoginButton(
    email: MutableState<String>,
    password: MutableState<String>,
    viewModel: LoginViewModel,
    networkStatusChecker: NetworkStatusChecker,
    showNoWifiSnackbar: MutableState<Boolean>
) {
    Button(
        onClick = {
            if (!networkStatusChecker.hasWifiConnection()) {
                // Trigger the snackbar display through LaunchedEffect
                showNoWifiSnackbar.value = true
            } else {
                viewModel.loginUser(email.value, password.value)
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF1565C0)
        )
    ) {
        Text("Login", modifier = Modifier.padding(8.dp))
    }
}
