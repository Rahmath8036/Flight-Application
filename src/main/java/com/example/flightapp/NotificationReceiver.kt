package com.example.flightapp

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * NotificationReceiver is a BroadcastReceiver that handles the reception of broadcast intents.
 * When an intent is received, it shows a notification with flight information.
 */
class NotificationReceiver : BroadcastReceiver() {
    /**
     * Called when the BroadcastReceiver is receiving an Intent broadcast.
     * This method extracts flight information from the intent and displays a notification.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            // Extract flight information from the intent
            val flightInfo = intent.getStringExtra("FLIGHT_INFO") ?: "Flight details unavailable"
            // Show the notification with the extracted flight information
            showNotification(context, flightInfo)
        }
    }

    /**
     * Displays a notification with the given text.
     *
     * @param context The Context in which the receiver is running.
     * @param text The text to display in the notification.
     */
    @SuppressLint("MissingPermission")
    private fun showNotification(context: Context, text: String) {
        val builder = NotificationCompat.Builder(context, "CHANNEL_ID")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Flight Reminder")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Show the notification using NotificationManagerCompat
        NotificationManagerCompat.from(context).notify(0, builder.build())
    }
}

