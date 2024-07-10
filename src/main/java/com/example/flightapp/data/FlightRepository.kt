package com.example.flightapp.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Repository for managing flight data operations.
 *
 * @property flightDao DAO for Flight entity.
 * @property bookedFlightDao DAO for BookedFlight entity.
 */
class FlightRepository(private val flightDao: FlightDao, private val bookedFlightDao: BookedFlightDao) {
    private val db = FirebaseFirestore.getInstance()

    /**
     * Syncs flights with Firebase Firestore in real-time.
     * Listens for changes in the "flights" collection and updates the local database.
     */
    fun syncFlightsWithFirebase() {
        db.collection("flights").addSnapshotListener { snapshots, e ->
            if (e != null) {
                // Handle the error appropriately
                return@addSnapshotListener
            }

            // Map documents to Flight objects and set their IDs
            val flights = snapshots?.mapNotNull { doc ->
                doc.toObject(Flight::class.java).apply { id = doc.id }
            }

            flights?.let {
                // Using a coroutine to handle database operations on the IO thread
                CoroutineScope(Dispatchers.IO).launch {
                    flightDao.insertAll(it)
                }
            }

            // In your repository or wherever you perform the database operations
//            fun insertFlights(flights: List<Flight>) {
//                CoroutineScope(Dispatchers.IO).launch {
//                    flightDao.insertAll(flights)
//                }
//            }
        }
    }

    /**
     * Fetches booked flights for a specific user from Firebase Firestore in real-time.
     * Listens for changes in the user's "bookedFlights" collection and updates the local database.
     *
     * @param userId StateFlow containing the user ID.
     */
    fun fetchBookedFlightsForUser(userId: StateFlow<String?>) {
        val bookedFlightsRef = db.collection("users").document(userId.toString()).collection("bookedFlights")
        bookedFlightsRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                // Handle the error appropriately
                Log.e("FlightRepository", "Listen failed.", e)
                return@addSnapshotListener
            }
            // Map documents to BookedFlight objects and set their IDs and user IDs
            val bookedFlights = snapshots?.mapNotNull { doc ->
                doc.toObject(BookedFlight::class.java).apply { id = doc.id; this.userId =
                    userId.toString()
                }
            }
            bookedFlights?.let {
                // Map documents to BookedFlight objects and set their IDs and user IDs
                CoroutineScope(Dispatchers.IO).launch {
                    bookedFlightDao.insertAll(it)
                }
            }
        }
    }
}
