package com.sulaiman.anilocal.data.local

import androidx.room.TypeConverter
import com.sulaiman.anilocal.domain.model.AnimeFormat
import com.sulaiman.anilocal.domain.model.AnimeSeason
import com.sulaiman.anilocal.domain.model.AnimeStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromStatus(status: AnimeStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): AnimeStatus = try {
        AnimeStatus.valueOf(value)
    } catch (e: Exception) {
        AnimeStatus.PLANNING
    }

    @TypeConverter
    fun fromFormat(format: AnimeFormat): String = format.name

    @TypeConverter
    fun toFormat(value: String): AnimeFormat = try {
        AnimeFormat.valueOf(value)
    } catch (e: Exception) {
        AnimeFormat.UNKNOWN
    }

    @TypeConverter
    fun fromSeason(season: AnimeSeason): String = season.name

    @TypeConverter
    fun toSeason(value: String): AnimeSeason = try {
        AnimeSeason.valueOf(value)
    } catch (e: Exception) {
        AnimeSeason.UNKNOWN
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> = try {
        Json.decodeFromString(value)
    } catch (e: Exception) {
        emptyList()
    }
}
