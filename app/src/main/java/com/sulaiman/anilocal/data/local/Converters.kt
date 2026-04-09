package com.sulaiman.anilocal.data.local

import androidx.room.TypeConverter
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
    fun fromList(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun toList(value: String): List<String> = try {
        Json.decodeFromString(value)
    } catch (e: Exception) {
        emptyList()
    }

    @TypeConverter
    fun fromLong(value: Long?): Long = value ?: 0L

    @TypeConverter
    fun toLong(value: Long): Long? = if (value == 0L) null else value
}