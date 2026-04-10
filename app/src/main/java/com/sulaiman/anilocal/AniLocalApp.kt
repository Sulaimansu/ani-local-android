package com.sulaiman.anilocal

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.sulaiman.anilocal.worker.AiringSyncWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AniLocalApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
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
