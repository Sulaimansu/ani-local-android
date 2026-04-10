package com.sulaiman.anilocal.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sulaiman.anilocal.domain.model.LocalAnime
import com.sulaiman.anilocal.domain.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SearchState())
    val state = _state.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.length > 2) {
            searchJob = viewModelScope.launch {
                delay(300) // debounce
                search(query)
            }
        } else {
            _state.update { it.copy(results = emptyList()) }
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
            repository.saveAnime(anime)
        }
    }
}
