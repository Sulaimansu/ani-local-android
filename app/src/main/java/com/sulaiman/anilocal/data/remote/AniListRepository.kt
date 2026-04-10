package com.sulaiman.anilocal.data.remote

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.sulaiman.anilocal.data.remote.graphql.GetAiringScheduleQuery
import com.sulaiman.anilocal.data.remote.graphql.GetMediaByIdQuery
import com.sulaiman.anilocal.data.remote.graphql.SearchAnimeQuery
import com.sulaiman.anilocal.data.remote.graphql.type.MediaFormat as GqlMediaFormat
import com.sulaiman.anilocal.data.remote.graphql.type.MediaSeason as GqlMediaSeason
import com.sulaiman.anilocal.data.remote.graphql.type.MediaStatus as GqlMediaStatus
import com.sulaiman.anilocal.data.remote.graphql.type.MediaType
import com.sulaiman.anilocal.data.remote.graphql.type.FuzzyDate as GqlFuzzyDate
import com.sulaiman.anilocal.domain.model.AiringAnime
import com.sulaiman.anilocal.domain.model.AnimeFormat
import com.sulaiman.anilocal.domain.model.AnimeSeason
import com.sulaiman.anilocal.domain.model.AnimeStatus
import com.sulaiman.anilocal.domain.model.LocalAnime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.ZoneOffset
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

            val data = response.data ?: run {
                emit(Result.success(emptyList()))
                return@flow
            }

            val mediaArray: Array<out SearchAnimeQuery.Medium?>? = data.page?.media
            val mediaList = mediaArray?.filterNotNull() ?: emptyList()
            val mapped = mediaList.map { it.toLocalAnime() }
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
                val medium = response.data?.media
                if (medium != null) {
                    Result.success(medium.toLocalAnimeFull())
                } else {
                    Result.failure(Exception("No data returned"))
                }
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

            val data = response.data ?: run {
                emit(Result.success(emptyList()))
                return@flow
            }

            val airingArray: Array<out GetAiringScheduleQuery.AiringSchedule?>? = data.airingSchedules
            val airingList = airingArray?.filterNotNull() ?: emptyList()
            val mapped = airingList.map { it.toAiringAnime() }
            emit(Result.success(mapped))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // ==================== Mapping ====================

    private fun SearchAnimeQuery.Medium.toLocalAnime(): LocalAnime {
        return LocalAnime(
            id = id ?: 0,
            titleRomaji = title?.romaji ?: "",
            titleEnglish = title?.english,
            titleNative = title?.native,
            description = description,
            status = AnimeStatus.PLANNING,
            mediaStatus = status?.name,
            format = format.toAnimeFormat(),
            episodes = episodes,
            duration = duration,
            season = season.toAnimeSeason(),
            seasonYear = seasonYear,
            coverImage = coverImage?.extraLarge ?: coverImage?.large,
            coverColor = coverImage?.color,
            bannerImage = bannerImage,
            genres = genres?.filterNotNull() ?: emptyList(),
            tags = tags?.mapNotNull { t -> t?.name } ?: emptyList(),
            synonyms = synonyms?.filterNotNull() ?: emptyList(),
            startDate = startDate.toEpochMillis(),
            endDate = null,
            nextAiringTime = nextAiringEpisode?.airingAt?.toLong()?.times(1000),
            nextEpisode = nextAiringEpisode?.episode,
            averageScore = averageScore,
            popularity = popularity,
            studios = studios?.nodes?.filterNotNull()?.mapNotNull { s -> s?.name } ?: emptyList(),
            siteUrl = siteUrl,
            relationsJson = this.relationsToJson()
        )
    }

    private fun SearchAnimeQuery.Medium.relationsToJson(): String? {
        return runCatching {
            val edges = relations?.edges ?: return@runCatching null
            val list = edges.filterNotNull().mapNotNull { edge ->
                edge?.let { e ->
                    val node = e.node
                    mapOf(
                        "type" to (e.relationType?.name ?: ""),
                        "id" to (node?.id ?: 0).toString(),
                        "title" to (node?.title?.romaji ?: ""),
                        "cover" to (node?.coverImage?.large ?: node?.coverImage?.extraLarge).orEmpty()
                    )
                }
            }
            kotlinx.serialization.json.Json.encodeToString(list)
        }.getOrNull()
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
            format = format.toAnimeFormat(),
            episodes = episodes,
            duration = duration,
            season = season.toAnimeSeason(),
            seasonYear = seasonYear,
            coverImage = coverImage?.extraLarge ?: coverImage?.large,
            coverColor = coverImage?.color,
            bannerImage = bannerImage,
            genres = genres?.filterNotNull() ?: emptyList(),
            tags = tags?.mapNotNull { t -> t?.name } ?: emptyList(),
            synonyms = synonyms?.filterNotNull() ?: emptyList(),
            startDate = startDate.toEpochMillis(),
            endDate = endDate.toEpochMillis(),
            nextAiringTime = nextAiringEpisode?.airingAt?.toLong()?.times(1000),
            nextEpisode = nextAiringEpisode?.episode,
            averageScore = averageScore,
            popularity = popularity,
            studios = studios?.nodes?.filterNotNull()?.mapNotNull { s -> s?.name } ?: emptyList(),
            externalLinks = runCatching {
                val links = externalLinks?.filterNotNull()?.map { link ->
                    mapOf("url" to link.url, "site" to link.site, "type" to link.type)
                } ?: emptyList()
                kotlinx.serialization.json.Json.encodeToString(links)
            }.getOrNull(),
            trailerId = trailer?.id,
            trailerSite = trailer?.site,
            siteUrl = siteUrl,
            relationsJson = this.relationsToJson()
        )
    }

    private fun GetMediaByIdQuery.Medium.relationsToJson(): String? {
        return runCatching {
            val edges = relations?.edges ?: return@runCatching null
            val list = edges.filterNotNull().mapNotNull { edge ->
                edge?.let { e ->
                    val node = e.node
                    mapOf(
                        "type" to (e.relationType?.name ?: ""),
                        "id" to (node?.id ?: 0).toString(),
                        "title" to (node?.title?.romaji ?: ""),
                        "titleEn" to (node?.title?.english).orEmpty(),
                        "cover" to (node?.coverImage?.large ?: node?.coverImage?.extraLarge).orEmpty(),
                        "status" to (node?.status?.name ?: ""),
                        "format" to (node?.format?.name ?: "")
                    )
                }
            }
            kotlinx.serialization.json.Json.encodeToString(list)
        }.getOrNull()
    }

    private fun GetAiringScheduleQuery.AiringSchedule.toAiringAnime(): AiringAnime {
        val m = media
        return AiringAnime(
            id = m?.id ?: 0,
            episode = episode ?: 0,
            airingAt = airingAt?.toLong()?.times(1000) ?: 0L,
            timeUntilAiring = timeUntilAiring ?: 0,
            titleRomaji = m?.title?.romaji ?: "",
            titleEnglish = m?.title?.english,
            coverImage = m?.coverImage?.extraLarge ?: m?.coverImage?.large,
            coverColor = m?.coverImage?.color,
            genres = m?.genres?.filterNotNull() ?: emptyList(),
            format = m?.format?.name,
            episodes = m?.episodes,
            averageScore = m?.averageScore,
            popularity = m?.popularity,
            nextAiringTime = m?.nextAiringEpisode?.airingAt?.toLong()?.times(1000),
            nextEpisode = m?.nextAiringEpisode?.episode,
            status = m?.status?.name,
            studios = m?.studios?.nodes?.filterNotNull()?.mapNotNull { s -> s?.name } ?: emptyList()
        )
    }

    // ==================== Enum helpers ====================

    private fun GqlMediaFormat?.toAnimeFormat(): AnimeFormat = when (this) {
        GqlMediaFormat.TV -> AnimeFormat.TV
        GqlMediaFormat.TV_SHORT -> AnimeFormat.TV_SHORT
        GqlMediaFormat.MOVIE -> AnimeFormat.MOVIE
        GqlMediaFormat.SPECIAL -> AnimeFormat.SPECIAL
        GqlMediaFormat.OVA -> AnimeFormat.OVA
        GqlMediaFormat.ONA -> AnimeFormat.ONA
        GqlMediaFormat.MUSIC -> AnimeFormat.MUSIC
        else -> AnimeFormat.UNKNOWN
    }

    private fun GqlMediaSeason?.toAnimeSeason(): AnimeSeason = when (this) {
        GqlMediaSeason.WINTER -> AnimeSeason.WINTER
        GqlMediaSeason.SPRING -> AnimeSeason.SPRING
        GqlMediaSeason.SUMMER -> AnimeSeason.SUMMER
        GqlMediaSeason.FALL -> AnimeSeason.FALL
        else -> AnimeSeason.UNKNOWN
    }

    private fun GqlFuzzyDate?.toEpochMillis(): Long? {
        val y = this?.year ?: return null
        val m = (this.month ?: 1).coerceIn(1, 12)
        val d = (this.day ?: 1).coerceIn(1, 28)
        return runCatching {
            LocalDate.of(y, m, d).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }.getOrNull()
    }
}
