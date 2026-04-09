package com.sulaiman.anilocal.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sulaiman.anilocal.domain.repository.AnimeRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class AiringSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: AnimeRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val library = repository.getLibrary().firstOrNull() ?: return Result.success()
            
            library.forEach { anime ->
                if (anime.nextAiringTime != null && anime.nextAiringTime < System.currentTimeMillis()) {
                    // In a real impl, fetch fresh airing data from AniList here
                    // For now, we just clear the expired timer or increment
                    // This placeholder logic prevents stale data
                    repository.updateAiringInfo(anime.id, null, null)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}