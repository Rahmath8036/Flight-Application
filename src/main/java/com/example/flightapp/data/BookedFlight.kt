package com.example.flightapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Define the BookedFlight entity and its table name for the Room database
@Entity(tableName = "bookedFlights")
data class BookedFlight(
    @PrimaryKey var id: String, // Primary key for the entity
    var userId: String, // Associate a flight with a user
    val origin: String, // Flight origin
    val destination: String, // Flight destination
    val departureDate: String, // Flight departure data
    val returnDate: String?, // Flight return date, nullable for one-way trips
    val price: Double, // Price of the flight
    val passengerCount: Int, // Number of passengers booked
    val tripType: String, // Type of trip: "one-way" or "return"
    val archived: Boolean, // Indicates if the flight is archived
    val deleted: Boolean // Indicates if the flight is soft-deleted
)
