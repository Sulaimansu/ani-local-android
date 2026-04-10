package com.sulaiman.anilocal.domain.repository

import com.sulaiman.anilocal.domain.model.AiringAnime
import com.sulaiman.anilocal.domain.model.LocalAnime
import kotlinx.coroutines.flow.Flow

interface AnimeRepository {
    // Remote operations
    fun searchAnime(query: String, page: Int): Flow<Result<List<LocalAnime>>>
    suspend fun getAnimeDetails(animeId: Int): Result<LocalAnime>
    fun getAiringToday(fromTimestamp: Int, toTimestamp: Int): Flow<Result<List<AiringAnime>>>
    suspend fun getAiringScheduleForAnimeIds(animeIds: List<Int>): Result<List<AiringAnime>>

    // Local operations
    suspend fun saveAnime(anime: LocalAnime)
    suspend fun updateAnime(anime: LocalAnime)
    suspend fun deleteAnime(id: Int)
    fun getLibrary(): Flow<List<LocalAnime>>
    fun getLibraryByStatus(status: String): Flow<List<LocalAnime>>
    fun getReleasingLibrary(): Flow<List<LocalAnime>>
    suspend fun getAnimeById(id: Int): LocalAnime?
    suspend fun updateAiringInfo(id: Int, nextAiringTime: Long?, nextEpisode: Int?)
    suspend fun updateAnimeMetadata(id: Int, status: String?, episodes: Int?, nextAiringTime: Long?, nextEpisode: Int?)
    suspend fun getAnimeAiringSoon(thresholdMs: Long, upperBoundMs: Long): List<LocalAnime>
}
