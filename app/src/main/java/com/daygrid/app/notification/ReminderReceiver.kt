package com.daygrid.app.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.daygrid.app.MainActivity
import com.daygrid.app.R

/**
 * Receives the alarm broadcast and shows the daily check-in notification.
 * Also reschedules for the next day.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        showNotification(context)
        // Reschedule for tomorrow (read saved hour/minute from SharedPrefs as DataStore can't be read synchronously in BroadcastReceiver)
        rescheduleForTomorrow(context)
    }

    private fun showNotification(context: Context) {
        val deepLinkIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("daygrid://checkin")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_REMINDER,
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("How was your day?")
            .setContentText("Did you learn something or make progress today?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_REMINDER, notification)
    }

    private fun rescheduleForTomorrow(context: Context) {
        // Read reminder prefs synchronously via SharedPreferences backup
        val prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("enabled", true)
        val hour = prefs.getInt("hour", 20)
        val minute = prefs.getInt("minute", 30)

        if (enabled) {
            val scheduler = NotificationScheduler(context)
            scheduler.scheduleReminder(hour, minute)
        }
    }
}
