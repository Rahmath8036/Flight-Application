package com.example.flightapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing user login and authentication states.
 */
class LoginViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _loginState = MutableStateFlow<LoginState?>(null)
    val loginState = _loginState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    // StateFlow to store the user ID
    private val _userId = MutableStateFlow<String?>(null)
    val userId = _userId.asStateFlow()

    /**
     * Logs in the user using Firebase Authentication.
     * Updates login state and user ID on success or failure.
     */
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            if (email.isBlank() || password.isBlank()) {
                _loginState.value = LoginState.Error("Email and password cannot be empty")
                return@launch
            }
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _isLoggedIn.value = true // Update isLoggedIn state
                    _loginState.value = LoginState.Success
                    _userId.value = auth.currentUser?.uid // Store user ID when login is successful
                } else {
                    _loginState.value = LoginState.Error(task.exception?.message ?: "Authentication failed")
                }
            }
        }
    }

    /**
     * Logs out the current user using Firebase Authentication.
     * Resets login state and clears user ID.
     */
    fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        _isLoggedIn.value = false // Update the isLoggedIn LiveData
        _userId.value = null // Clear user ID on logout
    }

    /**
     * Represents the possible states of the login process.
     */
    sealed class LoginState {
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
