package com.sulaiman.anilocal.data.local

import androidx.room.Dao
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

    @Query("SELECT * FROM local_anime ORDER BY addedAt DESC")
    fun getAllAnime(): Flow<List<LocalAnime>>

    @Query("SELECT * FROM local_anime WHERE status = :status ORDER BY addedAt DESC")
    fun getAnimeByStatus(status: String): Flow<List<LocalAnime>>

    @Query("DELETE FROM local_anime WHERE id = :id")
    suspend fun deleteAnime(id: Int)

    @Query("UPDATE local_anime SET nextAiringTime = :time, nextEpisode = :ep WHERE id = :id")
    suspend fun updateAiringInfo(id: Int, time: Long?, ep: Int?)
}