package com.sulaiman.anilocal

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.sulaiman.anilocal.worker.AiringSyncWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AniLocalApp : Application(), Configuration.Provider {
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setDefaultProcessName("${BuildConfig.APPLICATION_ID}:bg")
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleAiringSync()
    }

    private fun scheduleAiringSync() {
        val workManager = WorkManager.getInstance(this)
        val request = AiringSyncWorker.createPeriodicRequest()
        workManager.enqueueUniquePeriodicWork(
            "airing_sync",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
