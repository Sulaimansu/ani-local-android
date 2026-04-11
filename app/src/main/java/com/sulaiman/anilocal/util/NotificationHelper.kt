package com.sulaiman.anilocal.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
                enableVibration(true)
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

        val title = if (isImmediate) "🔴 Airing Now!" else "⏰ Episode Airing Soon!"
        val body = if (isImmediate) {
            "$animeTitle - Episode $episodeNumber"
        } else {
            "$animeTitle - Episode $episodeNumber airs in less than 10 minutes"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID_BASE + animeId, notification)
        }
    }

    fun cancelEpisodeNotification(context: Context, animeId: Int) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_BASE + animeId)
    }
}
