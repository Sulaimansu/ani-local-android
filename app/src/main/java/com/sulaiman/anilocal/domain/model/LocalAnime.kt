package com.sulaiman.anilocal.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AnimeStatus {
    WATCHING, COMPLETED, PLANNING, DROPPED, PAUSED
}

@Entity(tableName = "local_anime")
data class LocalAnime(
    @PrimaryKey val id: Int,
    val titleRomaji: String,
    val titleEnglish: String?,
    val titleNative: String?,
    val description: String?,
    val status: AnimeStatus,
    val episodes: Int?,
    val coverImage: String?,
    val coverColor: String?,
    val bannerImage: String?,
    val genres: List<String>,
    val tags: List<String>,
    val startDate: Long?,
    val nextAiringTime: Long?,
    val nextEpisode: Int?,
    val addedAt: Long = System.currentTimeMillis(),
    val relationsJson: String? = null
)