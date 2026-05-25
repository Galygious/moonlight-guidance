package org.arcanaforge.app.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import org.arcanaforge.app.MainActivity
import org.arcanaforge.app.R

object ScheduleNotificationHelper {
    const val CHANNEL_ID = "scheduled_readings"
    const val EXTRA_DECK_ID = "org.arcanaforge.app.extra.DECK_ID"
    const val EXTRA_LAYOUT_ID = "org.arcanaforge.app.extra.LAYOUT_ID"
    const val EXTRA_QUESTION = "org.arcanaforge.app.extra.QUESTION"
    const val EXTRA_OPEN_SCHEDULES = "org.arcanaforge.app.extra.OPEN_SCHEDULES"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Scheduled readings",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Reminders to prepare scheduled card readings."
        }
        manager.createNotificationChannel(channel)
    }

    fun canPostNotifications(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    fun showReadingReminder(
        context: Context,
        scheduleId: String,
        title: String,
        deckId: String,
        layoutId: String,
        question: String,
        deckName: String,
        layoutName: String,
    ) {
        if (!canPostNotifications(context)) return
        ensureChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DECK_ID, deckId)
            putExtra(EXTRA_LAYOUT_ID, layoutId)
            putExtra(EXTRA_QUESTION, question)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            scheduleId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val reminderTitle = "It's time to pull your $title"
        val reminderText = "Tap to draw your reading."
        val reminderDetails = "Tap to draw your reading with $deckName using $layoutName."
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(reminderTitle)
            .setContentText(reminderText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(reminderDetails))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(scheduleId.hashCode(), notification)
    }

    fun showScheduleError(
        context: Context,
        scheduleId: String,
        title: String,
    ) {
        if (!canPostNotifications(context)) return
        ensureChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_SCHEDULES, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            scheduleId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText("The selected deck or layout was deleted. Open schedules to update it.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(scheduleId.hashCode(), notification)
    }
}
