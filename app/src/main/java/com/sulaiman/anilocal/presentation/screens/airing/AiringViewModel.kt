package com.sulaiman.anilocal.presentation.screens.airing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sulaiman.anilocal.domain.model.AiringAnime
import com.sulaiman.anilocal.domain.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiringState(
    val airingAnime: List<AiringAnime> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AiringViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AiringState())
    val state = _state.asStateFlow()

    private var hasLoaded = false

    fun loadAiringToday() {
        if (hasLoaded) return
        hasLoaded = true
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Get next 24 hours from now
            val now = (System.currentTimeMillis() / 1000).toInt()
            val endOfDay = now + 86400

            repository.getAiringToday(now, endOfDay).collect { result ->
                result.onSuccess { list ->
                    _state.update {
                        it.copy(
                            airingAnime = list.sortedBy { a -> a.airingAt },
                            isLoading = false
                        )
                    }
                }.onFailure { error ->
                    _state.update { it.copy(error = error.message, isLoading = false) }
                }
            }
        }
    }
}
