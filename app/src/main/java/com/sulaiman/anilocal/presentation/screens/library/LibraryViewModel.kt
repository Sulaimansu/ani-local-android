package com.sulaiman.anilocal.presentation.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sulaiman.anilocal.domain.model.AnimeStatus
import com.sulaiman.anilocal.domain.model.LocalAnime
import com.sulaiman.anilocal.domain.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class LibraryState(
    val allAnime: List<LocalAnime> = emptyList(),
    val filteredAnime: List<LocalAnime> = emptyList(),
    val currentFilter: AnimeStatus? = null
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {
    private val _state = MutableStateFlow(LibraryState())
    val state = _state.asStateFlow()

    init {
        repository.getLibrary()
            .onEach { list ->
                _state.update {
                    it.copy(
                        allAnime = list,
                        filteredAnime = if (it.currentFilter == null) list else list.filter { a -> a.status == it.currentFilter }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun setFilter(status: AnimeStatus?) {
        _state.update {
            val filtered = if (status == null) it.allAnime else it.allAnime.filter { a -> a.status == status }
            it.copy(currentFilter = status, filteredAnime = filtered)
        }
    }

    fun updateStatus(id: Int, status: AnimeStatus) {
        viewModelScope.launch {
            val anime = repository.getAnimeById(id)
            anime?.let {
                repository.updateAnime(it.copy(status = status))
            }
        }
    }
}