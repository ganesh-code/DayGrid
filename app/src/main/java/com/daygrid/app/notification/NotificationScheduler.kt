package com.daygrid.app.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.daygrid.app.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

const val CHANNEL_ID_REMINDER = "daygrid_reminder"
const val NOTIFICATION_ID_REMINDER = 1001
const val REQUEST_CODE_REMINDER = 2001

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID_REMINDER,
            "Daily Reminder",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Evening check-in reminder"
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun scheduleReminder(hour: Int, minute: Int) {
        if (!hasNotificationPermission()) return

        val pendingIntent = buildReminderPendingIntent() ?: return

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1) // if past today's time, schedule tomorrow
        }

        // Cancel any existing alarm before setting new one
        cancelReminder()

        // Use setExactAndAllowWhileIdle for reliable delivery
        // Note: may be delayed by Android Doze mode. Device battery settings may affect delivery.
        try {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                // Fallback to inexact alarm — less reliable but no permission needed
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // SCHEDULE_EXACT_ALARM was revoked; fall back to inexact
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelReminder() {
        val pendingIntent = buildReminderPendingIntent() ?: return
        alarmManager.cancel(pendingIntent)
    }

    fun hasNotificationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun buildReminderPendingIntent(): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.daygrid.app.ACTION_REMINDER"
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
