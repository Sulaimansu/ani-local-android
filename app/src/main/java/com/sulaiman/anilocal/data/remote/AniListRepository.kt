package com.sulaiman.anilocal.data.remote

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.sulaiman.anilocal.data.remote.graphql.GetAiringScheduleQuery
import com.sulaiman.anilocal.data.remote.graphql.GetMediaByIdQuery
import com.sulaiman.anilocal.data.remote.graphql.SearchAnimeQuery
import com.sulaiman.anilocal.data.remote.graphql.type.MediaType
import com.sulaiman.anilocal.domain.model.AiringAnime
import com.sulaiman.anilocal.domain.model.AnimeFormat
import com.sulaiman.anilocal.domain.model.AnimeSeason
import com.sulaiman.anilocal.domain.model.AnimeStatus
import com.sulaiman.anilocal.domain.model.LocalAnime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AniListRepository @Inject constructor(
    private val apolloClient: ApolloClient
) {
    fun searchAnime(query: String, page: Int = 1): Flow<Result<List<LocalAnime>>> = flow {
        try {
            val response = apolloClient.query(
                SearchAnimeQuery(
                    search = Optional.presentIfNotNull(query),
                    page = Optional.presentIfNotNull(page),
                    type = Optional.presentIfNotNull(MediaType.ANIME)
                )
            ).execute()

            if (response.hasErrors()) {
                emit(Result.failure(Exception(response.errors?.firstOrNull()?.message)))
                return@flow
            }

            val mediaList = response.data?.page?.media?.filterNotNull() ?: emptyList()
            val mapped = mediaList.mapNotNull { it?.toLocalAnime() }
            emit(Result.success(mapped))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getAnimeDetails(animeId: Int): Result<LocalAnime> {
        return try {
            val response = apolloClient.query(
                GetMediaByIdQuery(id = animeId)
            ).execute()

            if (response.hasErrors()) {
                Result.failure(Exception(response.errors?.firstOrNull()?.message))
            } else {
                response.data?.media?.toLocalAnimeFull()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("No data returned"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAiringToday(
        airingAtGreater: Int,
        airingAtLesser: Int
    ): Flow<Result<List<AiringAnime>>> = flow {
        try {
            val response = apolloClient.query(
                GetAiringScheduleQuery(
                    airingAt_greater = Optional.presentIfNotNull(airingAtGreater),
                    airingAt_lesser = Optional.presentIfNotNull(airingAtLesser)
                )
            ).execute()

            if (response.hasErrors()) {
                emit(Result.failure(Exception(response.errors?.firstOrNull()?.message)))
                return@flow
            }

            val nodes = response.data?.airingSchedules?.filterNotNull() ?: emptyList()
            val mapped = nodes.mapNotNull { it?.toAiringAnime() }
            emit(Result.success(mapped))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // ---- Mapping extensions ----

    private fun SearchAnimeQuery.Medium.toLocalAnime(): LocalAnime {
        return LocalAnime(
            id = id ?: 0,
            titleRomaji = title?.romaji ?: "",
            titleEnglish = title?.english,
            titleNative = title?.native,
            description = description,
            status = AnimeStatus.PLANNING,
            mediaStatus = status?.name,
            format = format?.toAnimeFormat() ?: AnimeFormat.UNKNOWN,
            episodes = episodes,
            duration = duration,
            season = season?.toAnimeSeason() ?: AnimeSeason.UNKNOWN,
            seasonYear = seasonYear,
            coverImage = coverImage?.extraLarge ?: coverImage?.large,
            coverColor = coverImage?.color,
            bannerImage = bannerImage,
            genres = genres?.filterNotNull() ?: emptyList(),
            tags = tags?.mapNotNull { t -> t?.name } ?: emptyList(),
            synonyms = synonyms?.filterNotNull() ?: emptyList(),
            startDate = startDate?.toEpochMillis(),
            endDate = null,
            nextAiringTime = nextAiringEpisode?.airingAt?.toLong()?.times(1000),
            nextEpisode = nextAiringEpisode?.episode,
            averageScore = averageScore,
            popularity = popularity,
            studios = studios?.nodes?.filterNotNull()?.mapNotNull { it?.name } ?: emptyList(),
            siteUrl = siteUrl,
            relationsJson = null
        )
    }

    private fun GetMediaByIdQuery.Medium.toLocalAnimeFull(): LocalAnime {
        return LocalAnime(
            id = id ?: 0,
            idMal = idMal,
            titleRomaji = title?.romaji ?: "",
            titleEnglish = title?.english,
            titleNative = title?.native,
            description = description,
            status = AnimeStatus.PLANNING,
            mediaStatus = status?.name,
            format = format?.toAnimeFormat() ?: AnimeFormat.UNKNOWN,
            episodes = episodes,
            duration = duration,
            season = season?.toAnimeSeason() ?: AnimeSeason.UNKNOWN,
            seasonYear = seasonYear,
            coverImage = coverImage?.extraLarge ?: coverImage?.large,
            coverColor = coverImage?.color,
            bannerImage = bannerImage,
            genres = genres?.filterNotNull() ?: emptyList(),
            tags = tags?.mapNotNull { it?.name } ?: emptyList(),
            synonyms = synonyms?.filterNotNull() ?: emptyList(),
            startDate = startDate?.toEpochMillis(),
            endDate = endDate?.toEpochMillis(),
            nextAiringTime = nextAiringEpisode?.airingAt?.toLong()?.times(1000),
            nextEpisode = nextAiringEpisode?.episode,
            averageScore = averageScore,
            popularity = popularity,
            studios = studios?.nodes?.filterNotNull()?.mapNotNull { it?.name } ?: emptyList(),
            externalLinks = kotlin.runCatching {
                kotlinx.serialization.json.Json.encodeToString(
                    externalLinks?.filterNotNull()?.map { mapOf("url" to it.url, "site" to it.site, "type" to it.type) } ?: emptyList()
                )
            }.getOrNull(),
            trailerId = trailer?.id,
            trailerSite = trailer?.site,
            siteUrl = siteUrl,
            relationsJson = relationsToJson()
        )
    }

    private fun SearchAnimeQuery.Medium.relationsToJson(): String? {
        return kotlin.runCatching {
            kotlinx.serialization.json.Json.encodeToString(
                relations?.edges?.filterNotNull()?.mapNotNull { edge ->
                    edge?.let {
                        mapOf(
                            "type" to it.relationType?.name.orEmpty(),
                            "id" to (it.node?.id ?: 0),
                            "title" to (it.node?.title?.romaji ?: ""),
                            "cover" to (it.node?.coverImage?.large ?: it.node?.coverImage?.extraLarge).orEmpty()
                        )
                    }
                } ?: emptyList()
            )
        }.getOrNull()
    }

    private fun GetMediaByIdQuery.Medium.relationsToJson(): String? {
        return kotlin.runCatching {
            kotlinx.serialization.json.Json.encodeToString(
                relations?.edges?.filterNotNull()?.mapNotNull { edge ->
                    edge?.let {
                        mapOf(
                            "type" to it.relationType?.name.orEmpty(),
                            "id" to (it.node?.id ?: 0),
                            "title" to (it.node?.title?.romaji ?: ""),
                            "titleEn" to it.node?.title?.english,
                            "cover" to (it.node?.coverImage?.large ?: it.node?.coverImage?.extraLarge).orEmpty(),
                            "status" to it.node?.status?.name.orEmpty(),
                            "format" to it.node?.format?.name.orEmpty()
                        )
                    }
                } ?: emptyList()
            )
        }.getOrNull()
    }

    private fun GetAiringScheduleQuery.AiringSchedule.toAiringAnime(): AiringAnime {
        return AiringAnime(
            id = media?.id ?: 0,
            episode = episode ?: 0,
            airingAt = airingAt?.toLong()?.times(1000) ?: 0L,
            timeUntilAiring = timeUntilAiring ?: 0,
            titleRomaji = media?.title?.romaji ?: "",
            titleEnglish = media?.title?.english,
            coverImage = media?.coverImage?.extraLarge ?: media?.coverImage?.large,
            coverColor = media?.coverImage?.color,
            genres = media?.genres?.filterNotNull() ?: emptyList(),
            format = media?.format?.name,
            episodes = media?.episodes,
            averageScore = media?.averageScore,
            popularity = media?.popularity,
            nextAiringTime = media?.nextAiringEpisode?.airingAt?.toLong()?.times(1000),
            nextEpisode = media?.nextAiringEpisode?.episode,
            status = media?.status?.name,
            studios = media?.studios?.nodes?.filterNotNull()?.mapNotNull { it?.name } ?: emptyList()
        )
    }

    // ---- Enum helpers ----

    private fun SearchAnimeQuery.MediaFormat?.toAnimeFormat(): AnimeFormat = when (this) {
        SearchAnimeQuery.MediaFormat.TV -> AnimeFormat.TV
        SearchAnimeQuery.MediaFormat.TV_SHORT -> AnimeFormat.TV_SHORT
        SearchAnimeQuery.MediaFormat.MOVIE -> AnimeFormat.MOVIE
        SearchAnimeQuery.MediaFormat.SPECIAL -> AnimeFormat.SPECIAL
        SearchAnimeQuery.MediaFormat.OVA -> AnimeFormat.OVA
        SearchAnimeQuery.MediaFormat.ONA -> AnimeFormat.ONA
        SearchAnimeQuery.MediaFormat.MUSIC -> AnimeFormat.MUSIC
        else -> AnimeFormat.UNKNOWN
    }

    private fun GetMediaByIdQuery.MediaFormat?.toAnimeFormat(): AnimeFormat = when (this) {
        GetMediaByIdQuery.MediaFormat.TV -> AnimeFormat.TV
        GetMediaByIdQuery.MediaFormat.TV_SHORT -> AnimeFormat.TV_SHORT
        GetMediaByIdQuery.MediaFormat.MOVIE -> AnimeFormat.MOVIE
        GetMediaByIdQuery.MediaFormat.SPECIAL -> AnimeFormat.SPECIAL
        GetMediaByIdQuery.MediaFormat.OVA -> AnimeFormat.OVA
        GetMediaByIdQuery.MediaFormat.ONA -> AnimeFormat.ONA
        GetMediaByIdQuery.MediaFormat.MUSIC -> AnimeFormat.MUSIC
        else -> AnimeFormat.UNKNOWN
    }

    private fun SearchAnimeQuery.MediaSeason?.toAnimeSeason(): AnimeSeason = when (this) {
        SearchAnimeQuery.MediaSeason.WINTER -> AnimeSeason.WINTER
        SearchAnimeQuery.MediaSeason.SPRING -> AnimeSeason.SPRING
        SearchAnimeQuery.MediaSeason.SUMMER -> AnimeSeason.SUMMER
        SearchAnimeQuery.MediaSeason.FALL -> AnimeSeason.FALL
        else -> AnimeSeason.UNKNOWN
    }

    private fun GetMediaByIdQuery.MediaSeason?.toAnimeSeason(): AnimeSeason = when (this) {
        GetMediaByIdQuery.MediaSeason.WINTER -> AnimeSeason.WINTER
        GetMediaByIdQuery.MediaSeason.SPRING -> AnimeSeason.SPRING
        GetMediaByIdQuery.MediaSeason.SUMMER -> AnimeSeason.SUMMER
        GetMediaByIdQuery.MediaSeason.FALL -> AnimeSeason.FALL
        else -> AnimeSeason.UNKNOWN
    }

    private fun SearchAnimeQuery.FuzzyDate?.toEpochMillis(): Long? {
        val y = this?.year ?: return null
        val m = (this.month ?: 1).coerceIn(1, 12)
        val d = (this.day ?: 1).coerceIn(1, 28)
        return try {
            java.time.LocalDate.of(y, m, d).atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        } catch (_: Exception) {
            null
        }
    }

    private fun GetMediaByIdQuery.FuzzyDate?.toEpochMillis(): Long? {
        val y = this?.year ?: return null
        val m = (this.month ?: 1).coerceIn(1, 12)
        val d = (this.day ?: 1).coerceIn(1, 28)
        return try {
            java.time.LocalDate.of(y, m, d).atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        } catch (_: Exception) {
            null
        }
    }
}
