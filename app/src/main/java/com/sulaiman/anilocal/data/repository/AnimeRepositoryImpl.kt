package com.sulaiman.anilocal.data.repository

import com.apollographql.apollo.ApolloClient
import com.sulaiman.anilocal.data.local.AnimeDao
import com.sulaiman.anilocal.data.remote.AniListRepository
import com.sulaiman.anilocal.domain.model.AiringAnime
import com.sulaiman.anilocal.domain.model.LocalAnime
import com.sulaiman.anilocal.domain.repository.AnimeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AnimeRepositoryImpl @Inject constructor(
    apolloClient: ApolloClient,
    private val dao: AnimeDao
) : AnimeRepository {
    private val remote = AniListRepository(apolloClient)

    override fun searchAnime(query: String, page: Int): Flow<Result<List<LocalAnime>>> =
        remote.searchAnime(query, page)

    override suspend fun getAnimeDetails(animeId: Int): Result<LocalAnime> =
        remote.getAnimeDetails(animeId)

    override fun getAiringToday(fromTimestamp: Int, toTimestamp: Int): Flow<Result<List<AiringAnime>>> =
        remote.getAiringToday(fromTimestamp, toTimestamp)

    override suspend fun getAiringScheduleForAnimeIds(animeIds: List<Int>): Result<List<AiringAnime>> {
        return try {
            val results = mutableListOf<AiringAnime>()
            // For local airing schedule, use local DB data
            // This is mainly for fetching fresh airing data from API
            for (id in animeIds) {
                val details = remote.getAnimeDetails(id)
                details.onSuccess { anime ->
                    anime.nextAiringTime?.let { time ->
                        results.add(
                            AiringAnime(
                                id = anime.id,
                                episode = anime.nextEpisode ?: 0,
                                airingAt = time / 1000,
                                timeUntilAiring = ((time - System.currentTimeMillis()) / 1000).toInt(),
                                titleRomaji = anime.titleRomaji,
                                titleEnglish = anime.titleEnglish,
                                coverImage = anime.coverImage,
                                coverColor = anime.coverColor,
                                genres = anime.genres,
                                format = anime.format.name,
                                episodes = anime.episodes,
                                averageScore = anime.averageScore,
                                popularity = anime.popularity,
                                nextAiringTime = anime.nextAiringTime,
                                nextEpisode = anime.nextEpisode,
                                status = anime.mediaStatus,
                                studios = anime.studios
                            )
                        )
                    }
                }
            }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveAnime(anime: LocalAnime) = dao.insertAnime(anime)

    override suspend fun updateAnime(anime: LocalAnime) = dao.updateAnime(anime)

    override suspend fun deleteAnime(id: Int) = dao.deleteAnimeById(id)

    override fun getLibrary(): Flow<List<LocalAnime>> = dao.getAllAnime()

    override fun getLibraryByStatus(status: String): Flow<List<LocalAnime>> = dao.getAnimeByStatus(status)

    override fun getReleasingLibrary(): Flow<List<LocalAnime>> = dao.getReleasingAnime()

    override suspend fun getAnimeById(id: Int): LocalAnime? = dao.getAnimeById(id)

    override suspend fun updateAiringInfo(id: Int, nextAiringTime: Long?, nextEpisode: Int?) =
        dao.updateAiringInfo(id, nextAiringTime, nextEpisode)

    override suspend fun updateAnimeMetadata(
        id: Int,
        status: String?,
        episodes: Int?,
        nextAiringTime: Long?,
        nextEpisode: Int?
    ) = dao.updateAnimeMetadata(id, status, episodes, nextAiringTime, nextEpisode)

    override suspend fun getAnimeAiringSoon(thresholdMs: Long, upperBoundMs: Long): List<LocalAnime> =
        dao.getAnimeAiringBetween(thresholdMs, upperBoundMs)
}
