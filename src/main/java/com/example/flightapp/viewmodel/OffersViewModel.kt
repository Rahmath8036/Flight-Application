package com.example.flightapp.viewmodel

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate

// Extension property to create a DataStore instance for storing preferences
val Context.dataStore by preferencesDataStore(name = "favorites")

// ViewModel to manage offers data and user preferences
class OffersViewModel(private val context: Context) : ViewModel() {
    private val db = Firebase.firestore
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default_user"

    // Generate a unique key for each offer based on user ID and offer ID
    private fun favoriteKey(offerId: String) = booleanPreferencesKey("${userId}_$offerId")

    // StateFlow to hold the list of offers
    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: StateFlow<List<Offer>> = _offers

    // Initialize the ViewModel by fetching the offers
    init {
        fetchOffers()
    }

    /**
     * Fetch offers from Firestore and update the StateFlow
     */
    private fun fetchOffers() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val today = LocalDate.now().toString()
                // Fetch offers that have not expired
                val snapshot = db.collection("offers")
                    .whereGreaterThan("expiryDate", today)
                    .get()
                    .await()
                // Map Firestore documents to Offer objects
                val fetchedOffers = snapshot.documents.mapNotNull {
                    it.toObject(Offer::class.java)?.apply { id = it.id }
                }
                // Map Firestore documents to Offer objects
                withContext(Dispatchers.Main) {
                    _offers.value = fetchedOffers
                }
            } catch (e: Exception) {
                // Handle exceptions and update the StateFlow with an empty list
                withContext(Dispatchers.Main) {
                    _offers.value = emptyList() // Update UI even in case of an error
                }
                Log.e("OffersVM", "Error fetching offers: ${e.message}")
            }
        }
    }

    /**
     * Get the favorite status of an offer as a Flow
     */
    fun isFavorite(offerId: String): Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[favoriteKey(offerId)] ?: false
        }

    /**
     * Save the favorite status of an offer
     */
    fun saveFavoriteStatus(offerId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[favoriteKey(offerId)] = isFavorite
            }
        }
    }
}

// Data class to represent an offer
data class Offer(
    var id: String = "",
    val description: String = "",
    val discount: String = "",
    val expiryDate: String = "",
    var isFavorite: Boolean = false
)
