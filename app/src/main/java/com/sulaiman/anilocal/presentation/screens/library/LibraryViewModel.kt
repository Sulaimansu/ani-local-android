package com.sulaiman.anilocal.presentation.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sulaiman.anilocal.domain.model.AnimeStatus
import com.sulaiman.anilocal.domain.model.LocalAnime
import com.sulaiman.anilocal.domain.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryState(
    val allAnime: List<LocalAnime> = emptyList(),
    val filteredAnime: List<LocalAnime> = emptyList(),
    val currentFilter: AnimeStatus? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {
    private val _state = MutableStateFlow(LibraryState())
    val state = _state.asStateFlow()

    init {
        loadLibrary()
    }

    private fun loadLibrary() {
        repository.getLibrary()
            .onEach { list ->
                val sorted = list.sortedBy { it.titleRomaji.lowercase() }
                _state.update {
                    val filtered = if (it.currentFilter == null) {
                        sorted
                    } else {
                        sorted.filter { a -> a.status == it.currentFilter }
                    }
                    it.copy(
                        allAnime = sorted,
                        filteredAnime = filtered,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun setFilter(status: AnimeStatus?) {
        _state.update {
            val filtered = if (status == null) {
                it.allAnime
            } else {
                it.allAnime.filter { a -> a.status == status }
            }
            it.copy(currentFilter = status, filteredAnime = filtered)
        }
    }

    fun deleteAnime(id: Int) {
        viewModelScope.launch {
            repository.deleteAnime(id)
        }
    }

    fun refresh() {
        _state.update { it.copy(isLoading = true) }
        loadLibrary()
    }
}
