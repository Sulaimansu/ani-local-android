package com.sulaiman.anilocal.presentation.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sulaiman.anilocal.domain.model.LocalAnime
import com.sulaiman.anilocal.domain.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryState(
    val allAnime: List<LocalAnime> = emptyList(),
    val filteredAnime: List<LocalAnime> = emptyList(),
    val currentFilter: String? = null,
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
                _state.update {
                    val filtered = if (it.currentFilter == null) {
                        list
                    } else {
                        list.filter { a -> a.mediaStatus == it.currentFilter }
                    }
                    it.copy(
                        allAnime = list,
                        filteredAnime = filtered,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun setFilter(status: String?) {
        _state.update {
            val filtered = if (status == null) {
                it.allAnime
            } else {
                it.allAnime.filter { a -> a.mediaStatus == status }
            }
            it.copy(currentFilter = status, filteredAnime = filtered)
        }
    }

    fun refresh() {
        _state.update { it.copy(isLoading = true) }
        loadLibrary()
    }
}
