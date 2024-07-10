package com.example.flightapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory class to create an instance of OffersViewModel with a given context.
 * This allows the OffersViewModel to access the application context for data store operations.
 */
class OffersViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    /**
     * Creates an instance of the specified ViewModel class.
     * @param modelClass The class of the ViewModel to create.
     * @return An instance of the specified ViewModel class.
     * @throws IllegalArgumentException if the ViewModel class is unknown.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OffersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OffersViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}