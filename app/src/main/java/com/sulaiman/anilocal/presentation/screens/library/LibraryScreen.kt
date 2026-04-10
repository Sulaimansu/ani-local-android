package com.sulaiman.anilocal.presentation.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sulaiman.anilocal.domain.model.AnimeStatus
import com.sulaiman.anilocal.presentation.ui.theme.AniBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateToSearch: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Library") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToSearch) {
                Text("+")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = 0,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = AniBlue
            ) {
                Tab(
                    selected = state.currentFilter == null,
                    onClick = { viewModel.setFilter(null) },
                    text = { Text("All") }
                )
                AnimeStatus.values().forEach { status ->
                    Tab(
                        selected = state.currentFilter == status,
                        onClick = { viewModel.setFilter(status) },
                        text = { Text(status.name) }
                    )
                }
            }

            LazyColumn {
                items(state.filteredAnime) { anime ->
                    LibraryItem(anime) { newStatus ->
                        viewModel.updateStatus(anime.id, newStatus)
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryItem(anime: com.sulaiman.anilocal.domain.model.LocalAnime, onUpdateStatus: (AnimeStatus) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(anime.titleRomaji, style = MaterialTheme.typography.titleMedium)
            Text("Status: ${anime.status}", style = MaterialTheme.typography.bodySmall)
            
            anime.nextAiringTime?.let { time ->
                val remaining = (time - System.currentTimeMillis()) / 1000
                if (remaining > 0) {
                    Text("Next Ep ${anime.nextEpisode ?: "?"} in ${formatTime(remaining)}")
                }
            }

            Row {
                AnimeStatus.values().forEach {
                    Button(onClick = { onUpdateStatus(it) }) {
                        Text(it.name.take(1))
                    }
                }
            }
        }
    }
}

fun formatTime(seconds: Long): String {
    val d = seconds / 86400
    val h = (seconds % 86400) / 3600
    val m = (seconds % 3600) / 60
    return if (d > 0) "${d}d ${h}h" else if (h > 0) "${h}h ${m}m" else "${m}m"
}