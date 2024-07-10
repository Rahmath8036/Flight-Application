package com.example.flightapp.viewmodel

import NotificationHelper
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flightapp.NotificationReceiver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * ViewModel for managing booked flights and notifications.
 */
class BookedFlightsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    val bookedFlights = MutableStateFlow<List<Flight>>(emptyList())
    val fetchArchivedFlights=MutableStateFlow<List<Flight>>(emptyList())

    init {
        fetchBookedFlights()
        fetchArchivedFlights()
    }

    /**
     * Fetches the list of booked flights from Firestore.
     * Only includes flights that are not archived.
     */
    private fun fetchBookedFlights() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            viewModelScope.launch {
                db.collection("users")
                .document(user.uid)
                .collection("bookedFlights")
                .whereEqualTo("archived", false)  // Only fetch flights that are not archived
                .addSnapshotListener { value, e ->
                    if (e != null) {
                        println("Error fetching booked flights: ${e.message}")
                        return@addSnapshotListener
                    }
                    val fetchedFlights = mutableListOf<Flight>()
                    value?.documents?.forEach { document ->
                        document.toObject(Flight::class.java)?.let { flight ->
                            flight.id = document.id
                            fetchedFlights.add(flight)
                        }
                    }
                    bookedFlights.value = fetchedFlights
                }
            }
        }
    }

    /**
     * Archives a booked flight by setting its archived flag to true in Firestore.
     */
    fun archiveFlight(flight: Flight) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            db.collection("users")
                .document(user.uid)
                .collection("bookedFlights")
                .document(flight.id)
                .update("archived", true)
                .addOnSuccessListener {
                    println("Flight archived successfully")
                    fetchBookedFlights()  // Re-fetch the flights to update the UI
                }
                .addOnFailureListener { e ->
                    println("Error archiving flight: ${e.message}")
                }
        }
    }

    /**
     * Fetches the list of archived flights from Firestore.
     */
    private fun fetchArchivedFlights() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            viewModelScope.launch {
                db.collection("users")
                    .document(user.uid)
                    .collection("bookedFlights")
                    .whereEqualTo("archived", true)  // Only fetch archived flights
                    .whereEqualTo("deleted",false) // Exclude deleted flights
                    .addSnapshotListener { value, e ->
                        if (e != null) {
                            println("Error fetching archived flights: ${e.message}")
                            return@addSnapshotListener
                        }
                        val archivedFlights = mutableListOf<Flight>()
                        value?.documents?.forEach { document ->
                            document.toObject(Flight::class.java)?.let { flight ->
                                flight.id = document.id
                                archivedFlights.add(flight)
                            }
                        }
                        fetchArchivedFlights.value = archivedFlights
                    }
            }
        }
    }

    /**
     * Schedules notifications for all upcoming booked flights.
     * Notifications are scheduled to be triggered one day before the departure date.
     */
    fun scheduleNotificationsForUpcomingFlights(context: Context) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        bookedFlights.value.forEach { flight ->
            val departureDate = dateFormat.parse(flight.departureDate)
            if (departureDate != null) {
                val notificationTime = Calendar.getInstance().apply {
                    time = departureDate
                    add(Calendar.DAY_OF_YEAR, -1)  // Set to one day before the flight
                    set(Calendar.HOUR_OF_DAY, 8)    // Set to 8 AM
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                val currentTime = System.currentTimeMillis()
                Log.d("NotificationScheduler", "Scheduling notification for: ${flight.id}, Time: ${notificationTime.time}, Current Time: $currentTime")

                if (currentTime < notificationTime.timeInMillis) {
                    NotificationHelper.scheduleFlightNotification(context, flight, notificationTime.timeInMillis - currentTime)  // Time difference
                } else {
                    Log.d("NotificationScheduler", "Skipped scheduling for ${flight.id} as the trigger time is past")
                }
            } else {
                Log.d("NotificationScheduler", "Failed to parse date: ${flight.departureDate}")
            }
        }
    }

    /**
     * Schedules a notification for a specific flight when booking that flight.
     * Notification is scheduled to be triggered one day before the departure date.
     */
    fun scheduleNotificationsForFlight(context: Context,flight: Flight) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val departureDate = dateFormat.parse(flight.departureDate)
        if (departureDate != null) {
            val notificationTime = Calendar.getInstance().apply {
                time = departureDate
                add(Calendar.DAY_OF_YEAR, -1)
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val currentTime = System.currentTimeMillis()
            Log.d("NotificationScheduler", "Scheduling notification for: ${flight.id}, Time: ${notificationTime.time}, Current Time: $currentTime")

            if (currentTime < notificationTime.timeInMillis) {
                NotificationHelper.scheduleFlightNotification(context, flight, notificationTime.timeInMillis - currentTime)
            } else {
                Log.d("NotificationScheduler", "Skipped scheduling for ${flight.id} as the trigger time is past")
            }
        } else {
            Log.d("NotificationScheduler", "Failed to parse date: ${flight.departureDate}")
        }
    }

    /**
     * Cancels all scheduled notifications for booked flights when the user
     * denies permissions for enabling notifications.
     */
    fun cancelAllScheduledNotifications(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        bookedFlights.value.forEach { flight ->
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, flight.id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d("NotificationCancel", "Cancelled notification for flight ${flight.id}")
        }
    }

    /**
     * Soft deletes a flight by setting its deleted flag to true in Firestore.
     * This is done because the history of the users previous flights will be useful to the sky sailor company.
     */
    fun softDeleteFlight(flightId: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            viewModelScope.launch {
                db.collection("users")
                    .document(user.uid)
                    .collection("bookedFlights")
                    .document(flightId)
                    .update("deleted", true)
                    .addOnSuccessListener {
                        println("Flight marked as deleted successfully")
                        fetchArchivedFlights()  // Refresh the list archived flights to reflect changes
                    }
                    .addOnFailureListener { e ->
                        println("Error marking flight as deleted: ${e.message}")
                    }
            }
        }
    }
}


