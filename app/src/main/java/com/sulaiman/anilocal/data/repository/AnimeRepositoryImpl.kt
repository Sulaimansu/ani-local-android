package com.sulaiman.anilocal.data.repository

import com.apollographql.apollo.ApolloClient
import com.sulaiman.anilocal.data.local.AnimeDao
import com.sulaiman.anilocal.data.remote.AniListRepository
import com.sulaiman.anilocal.domain.model.LocalAnime
import com.sulaiman.anilocal.domain.repository.AnimeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AnimeRepositoryImpl @Inject constructor(
    apolloClient: ApolloClient,
    private val dao: AnimeDao
) : AnimeRepository {
    private val remote = AniListRepository(apolloClient)

    override fun searchAnime(query: String, page: Int): Flow<Result<List<LocalAnime>>> {
        return remote.searchAnime(query, page)
    }

    override suspend fun saveAnime(anime: LocalAnime) {
        dao.insertAnime(anime)
    }

    override suspend fun updateAnime(anime: LocalAnime) {
        dao.updateAnime(anime)
    }

    override suspend fun deleteAnime(id: Int) {
        dao.deleteAnime(id)
    }

    override fun getLibrary(): Flow<List<LocalAnime>> = dao.getAllAnime()

    override fun getAnimeByStatus(status: String): Flow<List<LocalAnime>> = dao.getAnimeByStatus(status)

    override suspend fun getAnimeById(id: Int): LocalAnime? = dao.getAnimeById(id)

    override suspend fun updateAiringInfo(id: Int, time: Long?, ep: Int?) {
        dao.updateAiringInfo(id, time, ep)
    }
}