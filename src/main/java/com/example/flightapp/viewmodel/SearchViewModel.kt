package com.example.flightapp.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Data class representing a Flight
data class Flight(
    var id: String = "",
    val origin: String = "",
    val destination: String = "",
    val departureDate: String = "",
    val returnDate: String? = null,
    val price: Double = 0.0,
    var passengerCount: Int = 1, // Default value set to 1
    val tripType: String = "One Way", // Default is "one-way"; other value is "return"
    var archived: Boolean = false, // Default value set to False
    var deleted: Boolean=false // Default value set to False

)

// ViewModel for handling flight search and booking logic
class SearchViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _flights = MutableStateFlow<List<Flight>>(emptyList())
    val flights = _flights.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    /**
     * Resets the search state.
     * Clears the list of flights and any error messages.
     */
    private fun resetSearch() {
        _flights.value = emptyList()
        _error.value = null
    }

    /**
     * Books a flight for the specified user.
     * @param flightId ID of the flight to be booked.
     * @param numberOfPassengers Number of passengers for the booking.
     * @param context Application context for accessing resources.
     * @param onSuccess Callback function to be executed on successful booking.
     */
    fun bookFlight(flightId: String, numberOfPassengers: Int, context: Context, onSuccess: () -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            db.runTransaction { transaction ->
                val flightRef = db.collection("flights").document(flightId)
                val snapshot = transaction.get(flightRef)
                val flight = snapshot.toObject(Flight::class.java) ?: throw IllegalStateException("Flight not found")

                if (flight.passengerCount >= numberOfPassengers) {
                    val newPassengerCount = flight.passengerCount - numberOfPassengers
                    transaction.update(flightRef, "passengerCount", newPassengerCount)

                    // Update flight details for booking
                    flight.passengerCount = numberOfPassengers
                    flight.archived = false  // Set isArchived to false when booking
                    flight.deleted=false

                    // Add updated flight to user's booked flights
                    transaction.set(db.collection("users").document(user.uid).collection("bookedFlights").document(), flight)

                    flight // Return the flight object to use it outside the transaction scope

                } else {
                    throw Exception("Not enough seats available")
                }
            }.addOnSuccessListener {
                transactionResult ->
                val bookedFlight = transactionResult as Flight
                println("Transaction successfully committed")
                if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                    // Schedule notifications for the flight the user is trying to book if the necessary permissions are granted
                    // and if they are: user friendly toast messages will be displayed.
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        val bookedFlightsViewModel = BookedFlightsViewModel()
                        bookedFlightsViewModel.scheduleNotificationsForFlight(context, bookedFlight) // Schedule notification for this flight
                        Toast.makeText(context, "Flight booked and notifications scheduled.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Flight booked but notification permissions are not granted.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Flight booked but notifications are disabled in system settings.", Toast.LENGTH_LONG).show()
                }
                onSuccess()
            }.addOnFailureListener { e ->
                println("Transaction failed: ${e.message}")
                _error.value = "Booking failed: ${e.message}"
            }
        }
    }

    /**
     * Searches for flights based on the specified criteria.
     * @param from Origin location.
     * @param to Destination location.
     * @param departureDate Departure date.
     * @param returnDate Return date (if applicable).
     * @param passengers Number of passengers.
     * @param tripType Type of trip ("One Way" or "Return").
     */
    fun searchFlights(from: String, to: String, departureDate: LocalDate, returnDate: LocalDate?, passengers: Int, tripType: String) {
        resetSearch() // Reset state before new search

        // Validate input fields to check if they are empty and if the entered dates are valid
        // If not, display error messages to the user so they can change their search accordingly.
        if (from.isBlank() || to.isBlank()) {
            _error.value = "Search Failed: Origin and destination cannot be empty"
            return
        }

        if (tripType == "Return" && returnDate != null) {
            if (returnDate.isBefore(departureDate)) {
                _error.value = "Search Failed: Return date must be after departure date"
                return
            }
        }
        viewModelScope.launch {
            // Format the data to yyyy-MM-dd
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val departureDateString = departureDate.format(dateFormatter)
            val departureDateEndString = departureDate.plusDays(3).format(dateFormatter)

            // Query the database to match the user's inputs and display output.
            val query = db.collection("flights")
                .whereEqualTo("origin", from)
                .whereEqualTo("destination", to)
                .whereEqualTo("tripType", tripType) // Filter by trip type

            if (tripType == "Return" && returnDate != null) {
                val returnDateString = returnDate.format(dateFormatter)
                val returnDateEndString = returnDate.plusDays(3).format(dateFormatter)
                query.whereGreaterThanOrEqualTo("returnDate", returnDateString)
                    .whereLessThanOrEqualTo("returnDate", returnDateEndString)
            }
            query.get().addOnSuccessListener { documents ->
                val results = documents.mapNotNull { doc ->
                    doc.toObject(Flight::class.java).apply {
                        id = doc.id  // Ensures the document ID is stored in the flight object
                    }.takeIf { flight ->
                        flight.passengerCount >= passengers &&
                                flight.departureDate >= departureDateString &&
                                flight.departureDate <= departureDateEndString
                    }
                }
                if (results.isEmpty()) {
                    _error.value = "Search Failed: No flights found matching your criteria, please ensure the input information is valid"
                } else {
                    _flights.value = results
                }
            }.addOnFailureListener { exception ->
                Log.e("FirestoreQuery", "Error fetching documents: ${exception.message}")
                _error.value = "Error retrieving flights: ${exception.message}"
                println("Error retrieving flights: ${exception.message}")
            }
        }
    }
}

