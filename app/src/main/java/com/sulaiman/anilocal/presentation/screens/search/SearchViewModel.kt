package com.sulaiman.anilocal.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sulaiman.anilocal.domain.model.LocalAnime
import com.sulaiman.anilocal.domain.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        if (query.length > 2) {
            search(query)
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.searchAnime(query, 1).collect { result ->
                result.onSuccess { list ->
                    _state.update { it.copy(results = list, isLoading = false) }
                }.onFailure {
                    _state.update { it.copy(error = it.message, isLoading = false) }
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