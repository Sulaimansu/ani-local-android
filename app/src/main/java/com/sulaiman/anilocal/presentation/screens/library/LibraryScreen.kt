package com.sulaiman.anilocal.presentation.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sulaiman.anilocal.R
import com.sulaiman.anilocal.presentation.ui.theme.AniBlue

@Composable
fun LibraryScreen(
    onNavigateToDetail: (Int) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("📚 Library") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )

        FilterChipsRow(
            currentFilter = state.currentFilter,
            onFilterChange = { viewModel.setFilter(it) }
        )

        if (state.filteredAnime.isEmpty() && !state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_library),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.filteredAnime, key = { it.id }) { anime ->
                    LibraryGridItem(
                        anime = anime,
                        onClick = { onNavigateToDetail(anime.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipsRow(
    currentFilter: String?,
    onFilterChange: (String?) -> Unit
) {
    val filters = listOf<Pair<String?, String>>(
        null to "All",
        "RELEASING" to "Airing",
        "FINISHED" to "Completed",
        "NOT_YET_RELEASED" to "Upcoming",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        filters.forEach { (value, label) ->
            FilterChip(
                selected = currentFilter == value,
                onClick = { onFilterChange(value) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}

@Composable
fun LibraryGridItem(
    anime: com.sulaiman.anilocal.domain.model.LocalAnime,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column {
            AsyncImage(
                model = anime.coverImage,
                contentDescription = anime.titleRomaji,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.7f),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = anime.titleRomaji,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    anime.format.let { fmt ->
                        if (fmt != com.sulaiman.anilocal.domain.model.AnimeFormat.UNKNOWN) {
                            Text(
                                text = "🎬 ${fmt.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AniBlue
                            )
                        }
                    }
                    if (anime.mediaStatus == "RELEASING") {
                        Text(
                            text = "🔴",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                if (anime.genres.isNotEmpty()) {
                    Text(
                        text = anime.genres.take(2).joinToString(", "),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
