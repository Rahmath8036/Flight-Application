package com.example.flightapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel for handling user sign-up logic
class SignUpViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _signUpState = MutableStateFlow<SignUpState?>(null)
    val signUpState = _signUpState.asStateFlow()

    /**
     * Registers a new user with the provided email and password.
     * @param email User's email address.
     * @param password User's chosen password.
     * @param confirmPassword Confirmation of the user's chosen password.
     */
    fun registerUser(email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            // Check for empty fields
            if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                _signUpState.value = SignUpState.Error("Fields cannot be empty")
                return@launch
            }
            // Check if passwords match
            if (password != confirmPassword) {
                _signUpState.value = SignUpState.Error("Passwords do not match")
                return@launch
            }
            // Attempt to create user with Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update sign-up state to success
                    _signUpState.value = SignUpState.Success
                } else {
                    // Update sign-up state to success
                    _signUpState.value = SignUpState.Error(task.exception?.message ?: "Registration failed")
                }
            }
        }
    }
    // Sealed class to represent the state of the sign-up process
    sealed class SignUpState {
        object Success : SignUpState()
        data class Error(val message: String) : SignUpState()
    }
}
