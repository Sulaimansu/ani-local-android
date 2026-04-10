package com.sulaiman.anilocal.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.sulaiman.anilocal.data.local.AnimeDao
import com.sulaiman.anilocal.data.remote.graphql.GetMediaByIdQuery
import com.sulaiman.anilocal.util.NotificationHelper
import com.sulaiman.anilocal.domain.model.LocalAnime
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker that:
 * 1. Fetches fresh airing info for all releasing anime in the library
 * 2. Checks for episodes airing in <10 min and sends notifications
 * 3. Updates local DB with fresh data
 */
@HiltWorker
class AiringSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val dao: AnimeDao,
    private val apolloClient: ApolloClient
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val releasingAnime = dao.getReleasingWithAiringInfo()
            if (releasingAnime.isEmpty()) {
                Log.d(TAG, "No releasing anime to sync")
                return@withContext Result.success()
            }

            val now = System.currentTimeMillis()
            val tenMinutesMs = 10 * 60 * 1000L
            val notificationThreshold = now + tenMinutesMs

            var successCount = 0
            var notificationCount = 0

            releasingAnime.forEach { anime ->
                try {
                    // Fetch fresh data from AniList
                    val response = apolloClient.query(
                        GetMediaByIdQuery(id = anime.id)
                    ).execute()

                    val media = response.data?.media
                    if (media != null) {
                        val newNextAiringTime = media.nextAiringEpisode?.airingAt?.toLong()?.times(1000)
                        val newNextEpisode = media.nextAiringEpisode?.episode
                        val newStatus = media.status?.name
                        val newEpisodes = media.episodes

                        // Update local DB
                        dao.updateAnimeMetadata(
                            id = anime.id,
                            status = newStatus,
                            episodes = newEpisodes,
                            nextAiringTime = newNextAiringTime,
                            nextEpisode = newNextEpisode
                        )

                        // Check if episode is airing soon (<10 min)
                        if (newNextAiringTime != null && newNextAiringTime <= notificationThreshold && newNextAiringTime > now) {
                            val minutesUntil = ((newNextAiringTime - now) / 60000).toInt()
                            NotificationHelper.showEpisodeNotification(
                                context = applicationContext,
                                animeId = anime.id,
                                animeTitle = anime.titleRomaji,
                                episodeNumber = newNextEpisode ?: 0,
                                minutesUntilAiring = minutesUntil
                            )
                            notificationCount++
                        }

                        // If already airing (within 1 min)
                        if (newNextAiringTime != null && newNextAiringTime <= now + 60000 && newNextAiringTime > now) {
                            NotificationHelper.showEpisodeNotification(
                                context = applicationContext,
                                animeId = anime.id,
                                animeTitle = anime.titleRomaji,
                                episodeNumber = newNextEpisode ?: 0,
                                minutesUntilAiring = 0,
                                isImmediate = true
                            )
                        }

                        successCount++
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing anime ${anime.id}: ${e.message}")
                }
            }

            Log.d(TAG, "Sync complete: $successCount anime updated, $notificationCount notifications sent")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Worker failed: ${e.message}")
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "AiringSyncWorker"
        private const val SYNC_INTERVAL_HOURS = 6L

        fun createPeriodicRequest(): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<AiringSyncWorker>(
                SYNC_INTERVAL_HOURS,
                java.util.concurrent.TimeUnit.HOURS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, java.util.concurrent.TimeUnit.MINUTES)
                .build()
        }
    }
}
