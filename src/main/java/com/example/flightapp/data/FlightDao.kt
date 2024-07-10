// FlightDao.kt
package com.example.flightapp.data

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface FlightDao {

    // Query to get all flights as a Cursor
    @Query("SELECT * FROM flights")
    fun getAllFlightsCursor(): Cursor

    // Query to get all flights as a List
    @Query("SELECT * FROM flights")
    fun getAllFlights(): List<Flight>

    // Insert multiple flights, replacing on conflict
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(flights: List<Flight>)

    // Delete all flights from the database
    @Query("DELETE FROM flights")
    fun clearFlights()

    // Insert a single flight, replacing on conflict, returns row ID
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(flight: Flight): Long

    // Query to get a flight by its ID as a Cursor
    @Query("SELECT * FROM flights WHERE id = :flightId")
    fun getFlightByIdCursor(flightId: Long): Cursor

    // Update a flight, returns number of rows affected
    @Update
    fun update(flight: Flight): Int

    // Delete a flight by its ID, returns number of rows deleted
    @Query("DELETE FROM flights WHERE id = :flightId")
    fun deleteFlight(flightId: String): Int
}