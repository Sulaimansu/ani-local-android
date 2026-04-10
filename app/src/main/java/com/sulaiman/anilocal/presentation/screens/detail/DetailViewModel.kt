package com.sulaiman.anilocal.presentation.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sulaiman.anilocal.domain.model.AnimeStatus
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

data class DetailState(
    val anime: LocalAnime? = null,
    val isInLibrary: Boolean = false,
    val userStatus: AnimeStatus? = null,
    val countdownText: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DetailState())
    val state = _state.asStateFlow()

    private var countdownJob: Job? = null

    fun loadAnimeDetails(animeId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Check local DB first
            val localAnime = repository.getAnimeById(animeId)

            if (localAnime != null) {
                _state.update {
                    it.copy(
                        anime = localAnime,
                        isInLibrary = true,
                        userStatus = localAnime.status,
                        isLoading = false
                    )
                }
            } else {
                // Fetch from API
                repository.getAnimeDetails(animeId).onSuccess { anime ->
                    _state.update {
                        it.copy(
                            anime = anime,
                            isInLibrary = false,
                            isLoading = false
                        )
                    }
                }.onFailure { error ->
                    _state.update { it.copy(error = error.message, isLoading = false) }
                }
            }

            startCountdown()
        }
    }

    fun saveToLibrary() {
        val currentAnime = _state.value.anime ?: return
        viewModelScope.launch {
            repository.saveAnime(currentAnime)
            _state.update {
                it.copy(isInLibrary = true, userStatus = AnimeStatus.PLANNING)
            }
        }
    }

    fun updateUserStatus(status: AnimeStatus) {
        val currentAnime = _state.value.anime ?: return
        viewModelScope.launch {
            val updated = currentAnime.copy(status = status)
            repository.updateAnime(updated)
            _state.update {
                it.copy(anime = updated, userStatus = status)
            }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        val anime = _state.value.anime ?: return
        if (anime.nextAiringTime == null || anime.mediaStatus == "FINISHED") {
            _state.update { it.copy(countdownText = null) }
            return
        }

        countdownJob = viewModelScope.launch {
            while (true) {
                val remaining = (anime.nextAiringTime!! - System.currentTimeMillis()) / 1000
                if (remaining <= 0) {
                    _state.update { it.copy(countdownText = "Airing now!") }
                    break
                }
                val d = remaining / 86400
                val h = (remaining % 86400) / 3600
                val m = (remaining % 3600) / 60
                val s = remaining % 60
                val text = if (d > 0) "${d}d ${h}h ${m}m"
                else if (h > 0) "${h}h ${m}m"
                else "${m}m ${s}s"
                _state.update { it.copy(countdownText = "Ep ${anime.nextEpisode ?: "?"} in $text") }
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
