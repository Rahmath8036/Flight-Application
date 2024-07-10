package com.example.flightapp.data

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.util.Log
import androidx.room.Room

class FlightContentProvider : ContentProvider() {

    private lateinit var flightDao: FlightDao
    private lateinit var bookedFlightDao: BookedFlightDao
    private lateinit var appDatabase: AppDatabase


    companion object {
        private const val FLIGHTS = 100
        private const val FLIGHT_ID = 101
        private const val BOOKED_FLIGHTS = 200
        private const val BOOKED_FLIGHT_ID = 201
        // URI matcher to handle different types of URIs
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI("com.example.flightapp.provider", "flights", FLIGHTS)
            addURI("com.example.flightapp.provider", "flights/#", FLIGHT_ID) // Handles specific flight by ID
            addURI("com.example.flightapp.provider", "bookedFlights", BOOKED_FLIGHTS)
            addURI("com.example.flightapp.provider", "bookedFlights/#", BOOKED_FLIGHT_ID)

        }
    }

    override fun onCreate(): Boolean {
        return try {
            // Initialize the Room database and DAOs
            appDatabase = Room.databaseBuilder(
                context!!,
                AppDatabase::class.java, "flight-database"
            ).build()
            flightDao = appDatabase.flightDao()
            bookedFlightDao = appDatabase.bookedFlightDao() // Ensure this is initialized
            Log.d("FlightContentProvider", "Database initialized successfully")
            true
        } catch (e: Exception) {
            Log.e("FlightContentProvider", "Failed to initialize database", e)
            false
        }
    }
    /**
     * Gets the MIME type for the provided URI.
     * @param uri the URI to query.
     * @return the MIME type string.
     */
    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            FLIGHTS -> "vnd.android.cursor.dir/vnd.com.example.flightapp.flights"
            FLIGHT_ID -> "vnd.android.cursor.item/vnd.com.example.flightapp.flight"
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
    }

    /**
     * Converts ContentValues to a Flight object.
     * @return a Flight object.
     */
    private fun ContentValues.toFlight(): Flight {
        return Flight(
            id = getAsString("id") ?: throw IllegalArgumentException("id is required"),
            origin = getAsString("origin") ?: "",
            destination = getAsString("destination") ?: "",
            departureDate = getAsString("departureDate") ?: "",
            returnDate = getAsString("returnDate"),
            price = getAsDouble("price") ?: 0.0,
            passengerCount = getAsInteger("passengerCount") ?: 1,
            tripType = getAsString("tripType") ?: "One Way",
            archived = getAsBoolean("archived") ?: false
        )
    }

    /**
     * Converts ContentValues to a BookedFlight object.
     * @return a BookedFlight object.
     */
    private fun ContentValues.toBookedFlight(): BookedFlight {
        return BookedFlight(
            id = getAsString("id") ?: throw IllegalArgumentException("id is required"),
            userId = getAsString("userId") ?: "",
            origin = getAsString("origin") ?: "",
            destination = getAsString("destination") ?: "",
            departureDate = getAsString("departureDate") ?: "",
            returnDate = getAsString("returnDate"),
            price = getAsDouble("price") ?: 0.0,
            passengerCount = getAsInteger("passengerCount") ?: 1,
            tripType = getAsString("tripType") ?: "One Way",
            archived = getAsBoolean("archived") ?: false,
            deleted = getAsBoolean("deleted") ?: false
        )
    }

    /**
     * Handles insert operations for flights and booked flights.
     * @param uri the URI to insert.
     * @param values the ContentValues to insert.
     * @return the URI of the newly inserted row.
     */
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return when (uriMatcher.match(uri)) {
            FLIGHTS -> {
                val flight = values?.toFlight()
                if (flight != null) {
                    val id = flightDao.insert(flight)
                    if (id != -1L) {
                        return ContentUris.withAppendedId(uri, id)
                    }
                }
                null
            }
            BOOKED_FLIGHTS -> {
                val bookedFlight = values?.toBookedFlight()
                if (bookedFlight != null) {
                    val id = bookedFlightDao.insert(bookedFlight)
                    if (id != -1L) {
                        return ContentUris.withAppendedId(uri, id)
                    }
                }
                null
            }
            else -> throw IllegalArgumentException("Invalid URI for insert operation: $uri")
        }
    }

    /**
     * Handles query operations for flights and booked flights.
     * @param uri the URI to query.
     * @param projection the list of columns to include in the resulting Cursor.
     * @param selection the selection criteria.
     * @param selectionArgs the selection arguments.
     * @param sortOrder the sort order.
     * @return a Cursor object containing the query results.
     */
    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return when (uriMatcher.match(uri)) {
            BOOKED_FLIGHTS -> bookedFlightDao.getBookedFlightsByUserIdCursor(selectionArgs?.firstOrNull() ?: "")
            BOOKED_FLIGHT_ID -> {
                val id = ContentUris.parseId(uri)
                bookedFlightDao.getBookedFlightByIdCursor(id)
            }
            FLIGHTS -> flightDao.getAllFlightsCursor() // Add this line to handle flight queries
            FLIGHT_ID -> {
                val id = ContentUris.parseId(uri)
                flightDao.getFlightByIdCursor(id)
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    /**
     * Handles delete operations for flights and booked flights.
     * @param uri the URI to delete.
     * @param selection the selection criteria.
     * @param selectionArgs the selection arguments.
     * @return the number of rows deleted.
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return when (uriMatcher.match(uri)) {
            FLIGHT_ID -> {
                val id = ContentUris.parseId(uri).toString()
                flightDao.deleteFlight(id)
            }
            BOOKED_FLIGHT_ID -> {
                val id = ContentUris.parseId(uri).toString()
                val cursor = bookedFlightDao.getBookedFlightByIdCursor(id.toLong())
                if (cursor != null && cursor.moveToFirst()) {
                    val isArchived = cursor.getInt(cursor.getColumnIndexOrThrow("archived")) == 1
                    cursor.close()
                    if (isArchived) {
                        bookedFlightDao.softDeleteBookedFlight(id)
                    } else {
                        0 // Flight is not archived, so don't delete
                    }
                } else {
                    0 // Flight not found
                }
            }
            else -> throw IllegalArgumentException("Invalid URI for delete operation: $uri")
        }
    }

    /**
     * Handles update operations for flights and booked flights.
     * @param uri the URI to update.
     * @param values the ContentValues to update.
     * @param selection the selection criteria.
     * @param selectionArgs the selection arguments.
     * @return the number of rows updated.
     */
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return when (uriMatcher.match(uri)) {
            FLIGHT_ID -> {
                val id = ContentUris.parseId(uri).toString()
                val flight = values?.apply { put("id", id) }?.toFlight()
                if (flight != null) {
                    flightDao.update(flight)
                } else {
                    0
                }
            }
            BOOKED_FLIGHT_ID -> {
                val id = ContentUris.parseId(uri).toString()
                val bookedFlight = values?.apply { put("id", id) }?.toBookedFlight()
                if (bookedFlight != null) {
                    bookedFlightDao.update(bookedFlight)
                } else {
                    0
                }
            }
            else -> throw IllegalArgumentException("Invalid URI for update operation: $uri")
        }
    }
}
