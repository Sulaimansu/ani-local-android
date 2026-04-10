package com.sulaiman.anilocal.data.remote

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.network.http.DefaultHttpEngine
import com.sulaiman.anilocal.data.remote.graphql.GetAiringScheduleQuery
import com.sulaiman.anilocal.data.remote.graphql.GetMediaByIdQuery
import com.sulaiman.anilocal.data.remote.graphql.SearchAnimeQuery
import com.sulaiman.anilocal.data.remote.graphql.type.MediaFormat
import com.sulaiman.anilocal.data.remote.graphql.type.MediaSeason
import com.sulaiman.anilocal.data.remote.graphql.type.MediaType
import com.sulaiman.anilocal.data.remote.graphql.type.FuzzyDate
import com.sulaiman.anilocal.domain.model.AiringAnime
import com.sulaiman.anilocal.domain.model.AnimeFormat
import com.sulaiman.anilocal.domain.model.AnimeSeason
import com.sulaiman.anilocal.domain.model.AnimeStatus
import com.sulaiman.anilocal.domain.model.LocalAnime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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

            val pageData = data.page
            val mediaList = pageData?.media ?: emptyList()
            val nonNullMedia = mediaList.filterNotNull()
            val mapped = nonNullMedia.map { m ->
                val fmt = m.format
                val sn = m.season
                val sd = m.startDate
                LocalAnime(
                    id = m.id ?: 0,
                    titleRomaji = (m.title?.romaji) ?: "",
                    titleEnglish = m.title?.english,
                    titleNative = m.title?.native,
                    description = m.description,
                    status = AnimeStatus.PLANNING,
                    mediaStatus = m.status?.name,
                    format = toAnimeFormat(fmt),
                    episodes = m.episodes,
                    duration = m.duration,
                    season = toAnimeSeason(sn),
                    seasonYear = m.seasonYear,
                    coverImage = m.coverImage?.extraLarge ?: m.coverImage?.large,
                    coverColor = m.coverImage?.color,
                    bannerImage = m.bannerImage,
                    genres = m.genres?.filterNotNull() ?: emptyList(),
                    tags = m.tags?.mapNotNull { t -> t?.name } ?: emptyList(),
                    synonyms = m.synonyms?.filterNotNull() ?: emptyList(),
                    startDate = fuzzyToMillisSearch(sd),
                    endDate = null,
                    nextAiringTime = m.nextAiringEpisode?.airingAt?.toLong()?.times(1000),
                    nextEpisode = m.nextAiringEpisode?.episode,
                    averageScore = m.averageScore,
                    popularity = m.popularity,
                    studios = m.studios?.nodes?.filterNotNull()?.mapNotNull { s -> s?.name } ?: emptyList(),
                    siteUrl = m.siteUrl,
                    relationsJson = relationsToJson(m.relations?.edges)
                )
            }
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
                val med = response.data?.media
                if (med != null) {
                    val fmt = med.format
                    val sn = med.season
                    val sd = med.startDate
                    val ed = med.endDate
                    Result.success(
                        LocalAnime(
                            id = med.id ?: 0,
                            idMal = med.idMal,
                            titleRomaji = (med.title?.romaji) ?: "",
                            titleEnglish = med.title?.english,
                            titleNative = med.title?.native,
                            description = med.description,
                            status = AnimeStatus.PLANNING,
                            mediaStatus = med.status?.name,
                            format = toAnimeFormat(fmt),
                            episodes = med.episodes,
                            duration = med.duration,
                            season = toAnimeSeason(sn),
                            seasonYear = med.seasonYear,
                            coverImage = med.coverImage?.extraLarge ?: med.coverImage?.large,
                            coverColor = med.coverImage?.color,
                            bannerImage = med.bannerImage,
                            genres = med.genres?.filterNotNull() ?: emptyList(),
                            tags = med.tags?.mapNotNull { t -> t?.name } ?: emptyList(),
                            synonyms = med.synonyms?.filterNotNull() ?: emptyList(),
                            startDate = fuzzyToMillisDetail(sd),
                            endDate = fuzzyToMillisDetail(ed),
                            nextAiringTime = med.nextAiringEpisode?.airingAt?.toLong()?.times(1000),
                            nextEpisode = med.nextAiringEpisode?.episode,
                            averageScore = med.averageScore,
                            popularity = med.popularity,
                            studios = med.studios?.nodes?.filterNotNull()?.mapNotNull { s -> s?.name } ?: emptyList(),
                            externalLinks = mapExternalLinks(med.externalLinks),
                            trailerId = med.trailer?.id,
                            trailerSite = med.trailer?.site,
                            siteUrl = med.siteUrl,
                            relationsJson = relationsToJsonDetail(med.relations?.edges)
                        )
                    )
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

            val airingData = data.airingSchedules
            val scheduleList = airingData?.nodes ?: emptyList()
            val nonNullSchedules = scheduleList.filterNotNull()
            val mapped = nonNullSchedules.map { s ->
                val m = s.media
                AiringAnime(
                    id = m?.id ?: 0,
                    episode = s.episode ?: 0,
                    airingAt = (s.airingAt?.toLong() ?: 0L).times(1000),
                    timeUntilAiring = s.timeUntilAiring ?: 0,
                    titleRomaji = (m?.title?.romaji) ?: "",
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
                    studios = m?.studios?.nodes?.filterNotNull()?.mapNotNull { st -> st?.name } ?: emptyList()
                )
            }
            emit(Result.success(mapped))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // ==================== Enum helpers ====================

    private fun toAnimeFormat(fmt: MediaFormat?): AnimeFormat = when (fmt) {
        MediaFormat.TV -> AnimeFormat.TV
        MediaFormat.TV_SHORT -> AnimeFormat.TV_SHORT
        MediaFormat.MOVIE -> AnimeFormat.MOVIE
        MediaFormat.SPECIAL -> AnimeFormat.SPECIAL
        MediaFormat.OVA -> AnimeFormat.OVA
        MediaFormat.ONA -> AnimeFormat.ONA
        MediaFormat.MUSIC -> AnimeFormat.MUSIC
        else -> AnimeFormat.UNKNOWN
    }

    private fun toAnimeSeason(sn: MediaSeason?): AnimeSeason = when (sn) {
        MediaSeason.WINTER -> AnimeSeason.WINTER
        MediaSeason.SPRING -> AnimeSeason.SPRING
        MediaSeason.SUMMER -> AnimeSeason.SUMMER
        MediaSeason.FALL -> AnimeSeason.FALL
        else -> AnimeSeason.UNKNOWN
    }

    private fun fuzzyToMillisSearch(d: FuzzyDate?): Long? {
        val y = d?.year ?: return null
        val mo = (d.month ?: 1).coerceIn(1, 12)
        val dy = (d.day ?: 1).coerceIn(1, 28)
        return runCatching {
            LocalDate.of(y, mo, dy).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }.getOrNull()
    }

    private fun fuzzyToMillisDetail(d: FuzzyDate?): Long? {
        val y = d?.year ?: return null
        val mo = (d.month ?: 1).coerceIn(1, 12)
        val dy = (d.day ?: 1).coerceIn(1, 28)
        return runCatching {
            LocalDate.of(y, mo, dy).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }.getOrNull()
    }

    // ==================== JSON helpers ====================

    private fun relationsToJson(edges: Array<out SearchAnimeQuery.Edge?>?): String? {
        return runCatching {
            if (edges == null) return@runCatching null
            buildJsonArray {
                edges.filterNotNull().forEach { edge ->
                    val n = edge.node
                    add(buildJsonObject {
                        put("type", edge.relationType?.name ?: "")
                        put("id", (n?.id ?: 0).toString())
                        put("title", (n?.title?.romaji) ?: "")
                        put("cover", (n?.coverImage?.large ?: n?.coverImage?.extraLarge).orEmpty())
                    })
                }
            }.toString()
        }.getOrNull()
    }

    private fun relationsToJsonDetail(edges: Array<out GetMediaByIdQuery.Edge?>?): String? {
        return runCatching {
            if (edges == null) return@runCatching null
            buildJsonArray {
                edges.filterNotNull().forEach { edge ->
                    val n = edge.node
                    add(buildJsonObject {
                        put("type", edge.relationType?.name ?: "")
                        put("id", (n?.id ?: 0).toString())
                        put("title", (n?.title?.romaji) ?: "")
                        put("titleEn", n?.title?.english ?: "")
                        put("cover", (n?.coverImage?.large ?: n?.coverImage?.extraLarge).orEmpty())
                        put("status", n?.status?.name ?: "")
                        put("format", n?.format?.name ?: "")
                    })
                }
            }.toString()
        }.getOrNull()
    }

    private fun mapExternalLinks(links: Array<out GetMediaByIdQuery.ExternalLink?>?): String? {
        return runCatching {
            if (links == null) return@runCatching null
            buildJsonArray {
                links.filterNotNull().forEach { link ->
                    add(buildJsonObject {
                        put("url", link.url)
                        put("site", link.site)
                        put("type", link.type)
                    })
                }
            }.toString()
        }.getOrNull()
    }

    companion object {
        fun createDefaultClient(): ApolloClient {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val okHttp = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
            return ApolloClient.Builder()
                .httpServerUrl("https://graphql.anilist.co")
                .httpEngine(DefaultHttpEngine(okHttp))
                .build()
        }
    }
}
