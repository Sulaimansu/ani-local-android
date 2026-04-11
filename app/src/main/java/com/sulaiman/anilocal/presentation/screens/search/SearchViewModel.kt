package com.sulaiman.anilocal.presentation.screens.search

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sulaiman.anilocal.domain.model.LocalAnime
import com.sulaiman.anilocal.domain.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchState(
    val query: String = "",
    val results: List<LocalAnime> = emptyList(),
    val libraryIds: Set<Int> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: AnimeRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(SearchState())
    val state = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadLibraryIds()
    }

    private fun loadLibraryIds() {
        viewModelScope.launch {
            repository.getLibrary().collect { list ->
                _state.update { it.copy(libraryIds = list.map { a -> a.id }.toSet()) }
            }
        }
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.length > 2) {
            searchJob = viewModelScope.launch {
                delay(300)
                search(query)
            }
        } else {
            // Don't clear results - keep cached
            _state.update { it.copy(results = it.results) }
        }
    }

    private fun search(query: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.searchAnime(query, 1).collect { result ->
                result.onSuccess { list ->
                    _state.update { it.copy(results = list, isLoading = false) }
                }.onFailure { error ->
                    _state.update { it.copy(error = error.message, isLoading = false) }
                }
            }
        }
    }

    fun saveAnime(anime: LocalAnime) {
        viewModelScope.launch {
            repository.saveAnime(anime, context)
            _state.update { it.copy(libraryIds = it.libraryIds + anime.id) }
        }
    }

    fun removeFromLibrary(id: Int) {
        viewModelScope.launch {
            repository.deleteAnime(id)
            _state.update { it.copy(libraryIds = it.libraryIds - id) }
        }
    }
}
