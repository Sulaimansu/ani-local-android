package com.sulaiman.anilocal.util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sulaiman.anilocal.R
import com.sulaiman.anilocal.presentation.MainActivity

object NotificationHelper {
    private const val CHANNEL_ID = "episode_reminders"
    private const val NOTIFICATION_ID_BASE = 1000

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Episode Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Get notified when episodes are about to air"
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun showEpisodeNotification(
        context: Context,
        animeId: Int,
        animeTitle: String,
        episodeNumber: Int,
        isImmediate: Boolean = false
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("animeId", animeId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            animeId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isImmediate) {
            context.getString(R.string.notification_title)
        } else {
            context.getString(R.string.notification_title)
        }
        val body = "$animeTitle - Episode $episodeNumber"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        if (hasPermission) {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID_BASE + animeId, notification)
        }
    }

    fun cancelEpisodeNotification(context: Context, animeId: Int) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_BASE + animeId)
    }

    /**
     * Schedule a notification for a specific time using AlarmManager.
     * This fires when the episode countdown reaches 0 (or <10 min before).
     */
    fun scheduleAiringNotification(context: Context, animeId: Int, animeTitle: String, episodeNumber: Int, airingAtMs: Long) {
        val intent = Intent(context, AiringAlarmReceiver::class.java).apply {
            putExtra("animeId", animeId)
            putExtra("animeTitle", animeTitle)
            putExtra("episodeNumber", episodeNumber)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            animeId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAtMs = airingAtMs - (10 * 60 * 1000) // 10 minutes before

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMs,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMs,
                pendingIntent
            )
        }
    }
}

/**
 * BroadcastReceiver that fires when an anime episode is about to air.
 */
class AiringAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val animeId = intent.getIntExtra("animeId", 0)
        val animeTitle = intent.getStringExtra("animeTitle") ?: ""
        val episodeNumber = intent.getIntExtra("episodeNumber", 0)

        NotificationHelper.showEpisodeNotification(
            context = context,
            animeId = animeId,
            animeTitle = animeTitle,
            episodeNumber = episodeNumber
        )
    }
}
