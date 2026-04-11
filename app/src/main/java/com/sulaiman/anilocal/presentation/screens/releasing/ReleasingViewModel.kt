package com.sulaiman.anilocal.presentation.screens.releasing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sulaiman.anilocal.domain.model.LocalAnime
import com.sulaiman.anilocal.domain.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReleasingState(
    val anime: List<LocalAnime> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class ReleasingViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ReleasingState())
    val state = _state.asStateFlow()

    init {
        loadReleasing()
    }

    private fun loadReleasing() {
        repository.getReleasingLibrary()
            .onEach { list ->
                val sorted = list.sortedWith(
                    compareBy<LocalAnime> { it.nextAiringTime == null }
                        .thenBy { it.nextAiringTime ?: Long.MAX_VALUE }
                        .thenBy { it.titleRomaji.lowercase() }
                )
                _state.update { it.copy(anime = sorted, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        loadReleasing()
    }
}
