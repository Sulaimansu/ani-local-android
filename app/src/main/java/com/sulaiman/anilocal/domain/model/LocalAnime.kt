package com.sulaiman.anilocal.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AnimeStatus {
    WATCHING, COMPLETED, PLANNING, DROPPED, PAUSED
}

enum class AnimeFormat {
    TV, TV_SHORT, MOVIE, SPECIAL, OVA, ONA, MUSIC, UNKNOWN
}

enum class AnimeSeason {
    WINTER, SPRING, SUMMER, FALL, UNKNOWN
}

@Entity(tableName = "local_anime")
data class LocalAnime(
    @PrimaryKey val id: Int,
    val idMal: Int? = null,
    val titleRomaji: String,
    val titleEnglish: String?,
    val titleNative: String?,
    val description: String?,
    val status: AnimeStatus,
    val mediaStatus: String? = null,
    val format: AnimeFormat = AnimeFormat.UNKNOWN,
    val episodes: Int?,
    val duration: Int? = null,
    val season: AnimeSeason = AnimeSeason.UNKNOWN,
    val seasonYear: Int? = null,
    val coverImage: String?,
    val coverColor: String?,
    val bannerImage: String?,
    val genres: List<String>,
    val tags: List<String>,
    val synonyms: List<String> = emptyList(),
    val startDate: Long?,
    val endDate: Long?,
    val nextAiringTime: Long?,
    val nextEpisode: Int?,
    val averageScore: Int? = null,
    val popularity: Int? = null,
    val studios: List<String> = emptyList(),
    val externalLinks: String? = null,
    val trailerId: String? = null,
    val trailerSite: String? = null,
    val siteUrl: String? = null,
    val relationsJson: String? = null,
    val userRating: Int? = null,
    val addedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long = 0L
)

data class AnimeRelation(
    val id: Int,
    val titleRomaji: String,
    val titleEnglish: String?,
    val relationType: String,
    val coverImage: String?,
    val status: String?,
    val format: String?
)

data class AiringAnime(
    val id: Int,
    val episode: Int,
    val airingAt: Long,
    val timeUntilAiring: Int,
    val titleRomaji: String,
    val titleEnglish: String?,
    val coverImage: String?,
    val coverColor: String?,
    val genres: List<String>,
    val format: String?,
    val episodes: Int?,
    val averageScore: Int?,
    val popularity: Int?,
    val nextAiringTime: Long?,
    val nextEpisode: Int?,
    val status: String?,
    val studios: List<String>
)
