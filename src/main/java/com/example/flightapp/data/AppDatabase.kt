// AppDatabase.kt
package com.example.flightapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Define the Room database with entities and version information
@Database(entities = [Flight::class, BookedFlight::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flightDao(): FlightDao
    abstract fun bookedFlightDao(): BookedFlightDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        // Singleton pattern to get the database instance
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Build the database and handle migration
                val instance = Room.databaseBuilder(context.applicationContext,
                    AppDatabase::class.java, "flight_database")
                    .fallbackToDestructiveMigration() // Handle migration strategy
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
