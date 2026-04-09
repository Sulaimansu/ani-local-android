package com.sulaiman.anilocal.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sulaiman.anilocal.domain.model.LocalAnime

@Database(entities = [LocalAnime::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AniDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao
}