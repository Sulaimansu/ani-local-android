package com.sulaiman.anilocal.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sulaiman.anilocal.domain.model.LocalAnime
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnime(anime: LocalAnime)

    @Update
    suspend fun updateAnime(anime: LocalAnime)

    @Query("SELECT * FROM local_anime WHERE id = :id")
    suspend fun getAnimeById(id: Int): LocalAnime?

    @Query("SELECT * FROM local_anime ORDER BY titleRomaji ASC")
    fun getAllAnime(): Flow<List<LocalAnime>>

    @Query("SELECT * FROM local_anime WHERE status = :status ORDER BY titleRomaji ASC")
    fun getAnimeByStatus(status: String): Flow<List<LocalAnime>>

    @Query("SELECT * FROM local_anime WHERE mediaStatus IN ('RELEASING', 'NOT_YET_RELEASED') ORDER BY nextAiringTime ASC")
    fun getReleasingAnime(): Flow<List<LocalAnime>>

    @Query("SELECT * FROM local_anime WHERE status = 'WATCHING' AND mediaStatus != 'FINISHED' ORDER BY nextAiringTime ASC")
    fun getWatchingAnime(): Flow<List<LocalAnime>>

    @Delete
    suspend fun deleteAnime(anime: LocalAnime)

    @Query("DELETE FROM local_anime WHERE id = :id")
    suspend fun deleteAnimeById(id: Int)

    @Query("UPDATE local_anime SET nextAiringTime = :nextAiringTime, nextEpisode = :nextEpisode, lastSyncedAt = :syncTime WHERE id = :id")
    suspend fun updateAiringInfo(id: Int, nextAiringTime: Long?, nextEpisode: Int?, syncTime: Long = System.currentTimeMillis())

    @Query("UPDATE local_anime SET mediaStatus = :status, episodes = :episodes, nextAiringTime = :nextAiringTime, nextEpisode = :nextEpisode, lastSyncedAt = :syncTime WHERE id = :id")
    suspend fun updateAnimeMetadata(
        id: Int,
        status: String?,
        episodes: Int?,
        nextAiringTime: Long?,
        nextEpisode: Int?,
        syncTime: Long = System.currentTimeMillis()
    )

    @Query("SELECT * FROM local_anime WHERE mediaStatus = 'RELEASING' AND nextAiringTime IS NOT NULL AND nextAiringTime > 0 ORDER BY nextAiringTime ASC")
    suspend fun getReleasingWithAiringInfo(): List<LocalAnime>

    @Query("SELECT * FROM local_anime WHERE nextAiringTime IS NOT NULL AND nextAiringTime > :threshold AND nextAiringTime <= :upperBound ORDER BY nextAiringTime ASC")
    suspend fun getAnimeAiringBetween(threshold: Long, upperBound: Long): List<LocalAnime>

    @Query("SELECT COUNT(*) FROM local_anime")
    suspend fun getAnimeCount(): Int
}
