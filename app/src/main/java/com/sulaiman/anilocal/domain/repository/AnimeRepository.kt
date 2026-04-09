package com.sulaiman.anilocal.domain.repository

import com.sulaiman.anilocal.domain.model.LocalAnime
import kotlinx.coroutines.flow.Flow

interface AnimeRepository {
    fun searchAnime(query: String, page: Int): Flow<Result<List<LocalAnime>>>
    suspend fun saveAnime(anime: LocalAnime)
    suspend fun updateAnime(anime: LocalAnime)
    suspend fun deleteAnime(id: Int)
    fun getLibrary(): Flow<List<LocalAnime>>
    fun getAnimeByStatus(status: String): Flow<List<LocalAnime>>
    suspend fun getAnimeById(id: Int): LocalAnime?
    suspend fun updateAiringInfo(id: Int, time: Long?, ep: Int?)
}