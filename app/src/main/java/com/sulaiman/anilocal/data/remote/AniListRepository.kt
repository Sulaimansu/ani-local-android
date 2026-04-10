package com.sulaiman.anilocal.data.remote

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.sulaiman.anilocal.data.remote.graphql.SearchAnimeQuery
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
                    page = Optional.presentIfNotNull(page)
                )
            ).execute()

            if (response.hasErrors()) {
                emit(Result.failure(Exception(response.errors?.firstOrNull()?.message)))
                return@flow
            }

            val mediaList = response.data?.page?.media?.filterNotNull() ?: emptyList()
            val mapped = mediaList.map { it!!.toLocalAnime() }
            emit(Result.success(mapped))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    private fun SearchAnimeQuery.Media.toLocalAnime(): LocalAnime {
        return LocalAnime(
            id = id!!,
            titleRomaji = title?.romaji ?: "",
            titleEnglish = title?.english,
            titleNative = title?.native,
            description = description,
            status = when (status) {
                SearchAnimeQuery.MediaStatus.FINISHED -> AnimeStatus.COMPLETED
                SearchAnimeQuery.MediaStatus.RELEASING -> AnimeStatus.WATCHING
                else -> AnimeStatus.PLANNING
            },
            episodes = episodes,
            coverImage = coverImage?.extraLarge,
            coverColor = coverImage?.color,
            bannerImage = bannerImage,
            genres = genres?.filterNotNull() ?: emptyList(),
            tags = tags?.mapNotNull { it?.name } ?: emptyList(),
            startDate = startDate?.year?.toLong(),
            nextAiringTime = nextAiringEpisode?.airingAt?.toLong()?.times(1000),
            nextEpisode = nextAiringEpisode?.episode,
            relationsJson = null
        )
    }
}