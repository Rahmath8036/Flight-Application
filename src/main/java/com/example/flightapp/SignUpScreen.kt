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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.flightapp.viewmodel.SignUpViewModel
import kotlinx.coroutines.delay

/**
 * SignUpScreen is a composable function for the user registration screen.
 * It handles user input, displays appropriate messages, and navigates back to login on success.
 *
 * @param viewModel ViewModel handling the signup logic.
 * @param onSignUpSuccess Callback for successful signup.
 * @param onNavigateBackToLogin Callback to navigate back to login screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(viewModel: SignUpViewModel, onSignUpSuccess: () -> Unit, onNavigateBackToLogin: () -> Unit) {
    val signUpState by viewModel.signUpState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val email = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }
    val confirmPassword = rememberSaveable { mutableStateOf("") }

    // Handle side effects based on signup state changes
    LaunchedEffect(signUpState) {
        when (signUpState) {
            is SignUpViewModel.SignUpState.Success -> {
                // Show success message and navigate to login screen after a delay
                snackbarHostState.showSnackbar(
                    message = "Registration successful. You will now proceed to login.",
                    duration = SnackbarDuration.Short
                )
                delay(1000) // Delay to allow user to read the message
                onNavigateBackToLogin() // Navigate back to the login screen
            }

            is SignUpViewModel.SignUpState.Error -> {
                // Show error message
                snackbarHostState.showSnackbar(
                    message = (signUpState as SignUpViewModel.SignUpState.Error).message,
                    duration = SnackbarDuration.Short
                )
            }

            else -> {}
        }
    }

    // Layout for the signup screen
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1565C0))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Display the app logo
        item {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(200.dp)
            )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        // Display the title
        item {
            Text(
                "Create Your Account",
                style = MaterialTheme.typography.headlineMedium.copy(color = Color.White)
            )
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }

        // Input field for email
        item {
            SignUpTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = "Email",
                imeAction = ImeAction.Next,
                placeholder = "Enter your email"
            )
        }
        // Input field for password
        item {
            SignUpTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = "Password",
                imeAction = ImeAction.Next,
                placeholder = "Enter your password"
            )
        }
        // Input field for confirm password
        item {
            SignUpTextField(
                value = confirmPassword.value,
                onValueChange = { confirmPassword.value = it },
                label = "Confirm Password",
                imeAction = ImeAction.Done,
                placeholder = "Re-enter your password"
            )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        // Add Register button
        item {
            Button(
                onClick = {
                    viewModel.registerUser(
                        email.value,
                        password.value,
                        confirmPassword.value
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1565C0)
                ),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Register", modifier = Modifier.padding(8.dp))
            }
        }
        // Back to login button
        item {
            Button(
                onClick = onNavigateBackToLogin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1565C0)
                ),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Back to Login", modifier = Modifier.padding(8.dp))
            }
        }
    }
    SnackbarHost(hostState = snackbarHostState)
}

/**
 * SignUpTextField is a reusable composable for the text fields in the signup screen.
 *
 * @param value The current text of the text field.
 * @param onValueChange Lambda to handle text changes.
 * @param label The label of the text field.
 * @param imeAction The IME action for the keyboard.
 * @param placeholder The placeholder text for the text field.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    imeAction: ImeAction,
    placeholder: String
) {
    // Define the colors for the text field
    val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        focusedBorderColor = Color.White,
        unfocusedBorderColor = Color.White,
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.White,
    )

    // Outlined text field with defined parameters
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        textStyle = TextStyle(color = Color.White),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = imeAction),
        colors = textFieldColors,
        placeholder = { Text(placeholder) }
    )
}
