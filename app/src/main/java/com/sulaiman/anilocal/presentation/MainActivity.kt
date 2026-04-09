package com.sulaiman.anilocal.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.sulaiman.anilocal.presentation.ui.theme.AniLocalTheme
import com.sulaiman.anilocal.worker.AiringSyncWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Schedule daily sync
        val syncRequest = PeriodicWorkRequestBuilder<AiringSyncWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "airing_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )

        setContent {
            AniLocalTheme {
                MainNavHost()
            }
        }
    }
}