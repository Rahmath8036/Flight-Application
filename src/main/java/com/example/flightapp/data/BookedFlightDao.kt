package com.example.flightapp.data

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

// Data Access Object for BookedFlight entity
@Dao
interface BookedFlightDao {

    // Insert multiple booked flights, replacing on conflict
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(bookedFlights: List<BookedFlight>)

    // Query to get booked flights by user ID as a Cursor
    @Query("SELECT * FROM bookedFlights WHERE userId = :userId")
    fun getBookedFlightsByUserIdCursor(userId: String): Cursor

    // Insert a single booked flight, replacing on conflict, returns row ID
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bookedFlight: BookedFlight): Long

    // Update a booked flight, returns number of rows affected
    @Update
    fun update(bookedFlight: BookedFlight): Int

    // Query to get a booked flight by its ID as a Cursor
    @Query("SELECT * FROM bookedFlights WHERE id = :id")
    fun getBookedFlightByIdCursor(id: Long): Cursor

    // Archive a booked flight by setting the archived flag
    @Query("UPDATE bookedFlights SET archived = :archived WHERE id = :id")
    fun archiveBookedFlight(id: String, archived: Boolean): Int

    // Soft delete a booked flight by setting the deleted flag
    @Query("UPDATE bookedFlights SET deleted = 1 WHERE id = :id")
    fun softDeleteBookedFlight(id: String): Int

}
