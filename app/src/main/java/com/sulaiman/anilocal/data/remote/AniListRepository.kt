package com.sulaiman.anilocal.data.remote

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.sulaiman.anilocal.data.remote.graphql.SearchAnimeQuery
import com.sulaiman.anilocal.data.remote.graphql.type.MediaType
import com.sulaiman.anilocal.data.remote.graphql.type.MediaStatus
import com.sulaiman.anilocal.domain.model.LocalAnime
import com.sulaiman.anilocal.domain.model.AnimeStatus
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
            val mapped = mediaList.map { m ->
                LocalAnime(
                    id = m.id ?: 0,
                    titleRomaji = m.title?.romaji ?: "",
                    titleEnglish = m.title?.english,
                    titleNative = m.title?.native,
                    description = m.description,
                    status = when (m.status) {
                        MediaStatus.FINISHED -> AnimeStatus.COMPLETED
                        MediaStatus.RELEASING -> AnimeStatus.WATCHING
                        else -> AnimeStatus.PLANNING
                    },
                    episodes = m.episodes,
                    coverImage = m.coverImage?.extraLarge,
                    coverColor = m.coverImage?.color,
                    bannerImage = m.bannerImage,
                    genres = m.genres?.filterNotNull() ?: emptyList(),
                    tags = m.tags?.mapNotNull { tag -> tag?.name } ?: emptyList(),
                    startDate = m.startDate?.year?.toLong(),
                    nextAiringTime = m.nextAiringEpisode?.airingAt?.toLong()?.times(1000),
                    nextEpisode = m.nextAiringEpisode?.episode,
                    relationsJson = null
                )
            }
            emit(Result.success(mapped))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}