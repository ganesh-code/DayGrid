package com.daygrid.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Reschedules the daily reminder after device boot or app update.
 * This ensures notifications survive device restarts.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
            val enabled = prefs.getBoolean("enabled", true)
            val hour = prefs.getInt("hour", 20)
            val minute = prefs.getInt("minute", 30)

            if (enabled) {
                val scheduler = NotificationScheduler(context)
                scheduler.createNotificationChannel()
                scheduler.scheduleReminder(hour, minute)
            }
        }
    }
}
