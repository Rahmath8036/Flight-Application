package com.example.flightapp

import android.content.ContentValues
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlightContentProviderTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun testInsertAndQueryFlight() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val contentResolver = context.contentResolver

        // Insert a flight
        val flightValues = ContentValues().apply {
            put("id", "1")
            put("origin", "New York")
            put("destination", "Los Angeles")
            put("departureDate", "2024-05-20")
            put("returnDate", "2024-05-30")
            put("price", 300.0)
            put("passengerCount", 1)
            put("tripType", "Round Trip")
            put("archived", false)
        }
        val uri = contentResolver.insert(Uri.parse("content://com.example.flightapp.provider/flights"), flightValues)
        assertEquals(Uri.parse("content://com.example.flightapp.provider/flights/1"), uri)

        // Query the flight
        val cursor = contentResolver.query(
            Uri.parse("content://com.example.flightapp.provider/flights"),
            null, null, null, null
        )

        cursor?.apply {
            assertEquals(1, count)
            moveToFirst()
            assertEquals("New York", getString(getColumnIndexOrThrow("origin")))
            assertEquals("Los Angeles", getString(getColumnIndexOrThrow("destination")))
            close()
        }
    }

    @Test
    fun testInsertAndQueryBookedFlight() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val contentResolver = context.contentResolver

        // Insert a booked flight
        val bookedFlightValues = ContentValues().apply {
            put("id", "1")
            put("userId", "user123")
            put("origin", "San Francisco")
            put("destination", "Chicago")
            put("departureDate", "2024-06-10")
            put("returnDate", "2024-06-20")
            put("price", 250.0)
            put("passengerCount", 1)
            put("tripType", "Round Trip")
            put("archived", false)
            put("deleted", false)
        }
        val uri = contentResolver.insert(Uri.parse("content://com.example.flightapp.provider/bookedFlights"), bookedFlightValues)
        assertEquals(Uri.parse("content://com.example.flightapp.provider/bookedFlights/1"), uri)

        // Query the booked flight
        val cursor = contentResolver.query(
            Uri.parse("content://com.example.flightapp.provider/bookedFlights"),
            null, "userId = ?", arrayOf("user123"), null
        )

        cursor?.apply {
            assertEquals(1, count)
            moveToFirst()
            assertEquals("San Francisco", getString(getColumnIndexOrThrow("origin")))
            assertEquals("Chicago", getString(getColumnIndexOrThrow("destination")))
            close()
        }
    }
    @Test
    fun testUpdateFlight() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val contentResolver = context.contentResolver

        // Insert a flight
        val flightValues = ContentValues().apply {
            put("id", "1")
            put("origin", "New York")
            put("destination", "Los Angeles")
            put("departureDate", "2024-05-20")
            put("returnDate", "2024-05-30")
            put("price", 300.0)
            put("passengerCount", 1)
            put("tripType", "Round Trip")
            put("archived", false)
        }
        val uri = contentResolver.insert(Uri.parse("content://com.example.flightapp.provider/flights"), flightValues)
        assertEquals(Uri.parse("content://com.example.flightapp.provider/flights/1"), uri)

        // Update the flight
        val updatedFlightValues = ContentValues().apply {
            put("id", "1") // Ensure the ID is included
            put("origin", "New York")
            put("destination", "Los Angeles")
            put("departureDate", "2024-05-20")
            put("returnDate", "2024-05-30")
            put("price", 350.0)
            put("passengerCount", 1)
            put("tripType", "Round Trip")
            put("archived", false)
        }
        val updateUri = Uri.parse("content://com.example.flightapp.provider/flights/1")
        val rowsUpdated = contentResolver.update(updateUri, updatedFlightValues, null, null)
        assertEquals(1, rowsUpdated)

        // Query the flight to check update
        val cursor = contentResolver.query(
            updateUri,
            null, null, null, null
        )

        cursor?.apply {
            assertEquals(1, count)
            moveToFirst()
            assertEquals(350.0, getDouble(getColumnIndexOrThrow("price")), 0.0)
            close()
        }
    }

    @Test
    fun testArchiveBookedFlight() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val contentResolver = context.contentResolver

        // Insert a booked flight
        val bookedFlightValues = ContentValues().apply {
            put("id", "1")
            put("userId", "user123")
            put("origin", "San Francisco")
            put("destination", "Chicago")
            put("departureDate", "2024-06-10")
            put("returnDate", "2024-06-20")
            put("price", 250.0)
            put("passengerCount", 1)
            put("tripType", "Round Trip")
            put("archived", false)
            put("deleted", false)
        }
        val uri = contentResolver.insert(Uri.parse("content://com.example.flightapp.provider/bookedFlights"), bookedFlightValues)
        assertEquals(Uri.parse("content://com.example.flightapp.provider/bookedFlights/1"), uri)

        // Archive the booked flight
        val updateValues = ContentValues().apply {
            put("id", "1") // Ensure the ID is included
            put("archived", true)
        }
        val updateUri = Uri.parse("content://com.example.flightapp.provider/bookedFlights/1")
        val rowsUpdated = contentResolver.update(updateUri, updateValues, null, null)
        assertEquals(1, rowsUpdated)

        // Query the booked flight to check archive status
        val cursor = contentResolver.query(
            updateUri,
            null, null, null, null
        )

        cursor?.apply {
            assertEquals(1, count)
            moveToFirst()
            assertEquals(true, getInt(getColumnIndexOrThrow("archived")) == 1)
            close()
        }
    }

    @Test
    fun testDeleteBookedFlight() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val contentResolver = context.contentResolver

        // Insert a non-archived booked flight
        val nonArchivedBookedFlightValues = ContentValues().apply {
            put("id", "1")
            put("userId", "user123")
            put("origin", "San Francisco")
            put("destination", "Chicago")
            put("departureDate", "2024-06-10")
            put("returnDate", "2024-06-20")
            put("price", 250.0)
            put("passengerCount", 1)
            put("tripType", "Round Trip")
            put("archived", false)
            put("deleted", false)
        }
        val nonArchivedUri = contentResolver.insert(Uri.parse("content://com.example.flightapp.provider/bookedFlights"), nonArchivedBookedFlightValues)
        assertEquals(Uri.parse("content://com.example.flightapp.provider/bookedFlights/1"), nonArchivedUri)

        // Attempt to delete the non-archived booked flight (should not delete)
        val nonArchivedDeleteUri = Uri.parse("content://com.example.flightapp.provider/bookedFlights/1")
        val nonArchivedRowsDeleted = contentResolver.delete(nonArchivedDeleteUri, null, null)
        assertEquals(0, nonArchivedRowsDeleted)

        // Insert an archived booked flight
        val archivedBookedFlightValues = ContentValues().apply {
            put("id", "2")
            put("userId", "user123")
            put("origin", "San Francisco")
            put("destination", "Chicago")
            put("departureDate", "2024-06-10")
            put("returnDate", "2024-06-20")
            put("price", 250.0)
            put("passengerCount", 1)
            put("tripType", "Round Trip")
            put("archived", true)
            put("deleted", false)
        }
        val archivedUri = contentResolver.insert(Uri.parse("content://com.example.flightapp.provider/bookedFlights"), archivedBookedFlightValues)
        assertEquals(Uri.parse("content://com.example.flightapp.provider/bookedFlights/2"), archivedUri)

        // Delete the archived booked flight (soft delete)
        val deleteUri = Uri.parse("content://com.example.flightapp.provider/bookedFlights/2")
        val rowsDeleted = contentResolver.delete(deleteUri, null, null)
        assertEquals(1, rowsDeleted)

        // Query the booked flight to ensure it's soft deleted
        val cursor = contentResolver.query(
            deleteUri,
            null, null, null, null
        )

        cursor?.apply {
            assertEquals(1, count)
            moveToFirst()
            assertEquals(true, getInt(getColumnIndexOrThrow("deleted")) == 1)
            close()
        }
    }
}
