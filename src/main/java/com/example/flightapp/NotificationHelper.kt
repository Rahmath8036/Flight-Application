
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.flightapp.NotificationReceiver
import com.example.flightapp.viewmodel.Flight
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * NotificationHelper is an object responsible for scheduling flight notifications.
 * It contains methods to schedule notifications for flights and for testing purposes.
 */
object NotificationHelper {

    /**
     * Schedules a notification for a flight.
     *
     * @param context The application context.
     * @param flight The Flight object containing flight details.
     * @param timeBeforeFlight The time before the flight to trigger the notification.
     */
    @SuppressLint("ScheduleExactAlarm")
    fun scheduleFlightNotification(context: Context, flight: Flight, timeBeforeFlight: Long) {

        // Obtain the AlarmManager system service
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Create a PendingIntent to be triggered at the scheduled time
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("FLIGHT_ID", flight.id)
            putExtra("FLIGHT_INFO", "Flight from ${flight.origin} to ${flight.destination} is tomorrow at 8:00 AM!")
        }
        // Create a PendingIntent to be triggered at the scheduled time
        val pendingIntent = PendingIntent.getBroadcast(
            context, flight.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Format the departure date and set the notification time to one day before the flight
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val departureDate = dateFormat.parse(flight.departureDate)
        if (departureDate != null) {
            val notificationCalendar = Calendar.getInstance().apply {
                time = departureDate
                add(Calendar.DAY_OF_MONTH, -1)  // Set to one day before the flight
                set(Calendar.HOUR_OF_DAY, 8)    // Set to 8 AM
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val triggerTime = notificationCalendar.timeInMillis
            val currentTime = System.currentTimeMillis()
            Log.d("NotificationHelper", "Setting alarm for Flight ${flight.id} at: ${notificationCalendar.time}, Trigger Time: $triggerTime, Current Time: $currentTime")
            // Schedule the alarm if the current time is before the trigger time
            if (currentTime < triggerTime) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                Log.d("NotificationHelper", "Skipped scheduling for ${flight.id} as the trigger time is past")
            }
        } else {
            Log.d("NotificationHelper", "Failed to parse departure date for Flight ${flight.id}")
        }
    }
}
