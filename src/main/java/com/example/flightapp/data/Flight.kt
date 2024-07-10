// Flight.kt
package com.example.flightapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Define Flight entity with table name "flights"
@Entity(tableName = "flights")
data class Flight(
    @PrimaryKey var id: String = "", // Primary key
    val origin: String = "", // Flight origin
    val destination: String = "", // Flight destination
    val departureDate: String = "", // Departure date
    val returnDate: String? = null, // Return date, nullable
    val price: Double = 0.0, // Flight price
    val passengerCount: Int = 1, // Number of passengers
    val tripType: String = "One Way", // Trip type (one way or return)
    val archived: Boolean = false // Archived flag
)

