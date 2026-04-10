package com.sulaiman.anilocal.di

import android.content.Context
import androidx.room.Room
import com.apollographql.apollo.ApolloClient
import com.sulaiman.anilocal.data.local.AniDatabase
import com.sulaiman.anilocal.data.local.AnimeDao
import com.sulaiman.anilocal.data.repository.AnimeRepositoryImpl
import com.sulaiman.anilocal.domain.repository.AnimeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApolloClient(): ApolloClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        return ApolloClient.Builder()
            .httpServerUrl("https://graphql.anilist.co")
            .okHttpClient(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AniDatabase {
        return Room.databaseBuilder(
            context,
            AniDatabase::class.java,
            "ani_local_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideAnimeDao(database: AniDatabase): AnimeDao = database.animeDao()

    @Provides
    @Singleton
    fun provideAnimeRepository(
        apolloClient: ApolloClient,
        dao: AnimeDao
    ): AnimeRepository {
        return AnimeRepositoryImpl(apolloClient, dao)
    }
}
