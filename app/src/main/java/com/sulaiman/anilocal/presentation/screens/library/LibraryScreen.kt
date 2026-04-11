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
import com.sulaiman.anilocal.domain.model.AnimeFormat
import com.sulaiman.anilocal.domain.model.AnimeStatus
import com.sulaiman.anilocal.presentation.ui.theme.AniBlue
import com.sulaiman.anilocal.util.ImageLoaderUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateToDetail: (Int) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {

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
                        context = context,
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
    currentFilter: AnimeStatus?,
    onFilterChange: (AnimeStatus?) -> Unit
) {
    val filters = listOf<Pair<AnimeStatus?, String>>(
        null to "All",
        AnimeStatus.WATCHING to "Watching",
        AnimeStatus.PLANNING to "Planning",
        AnimeStatus.COMPLETED to "Completed",
        AnimeStatus.DROPPED to "Dropped",
        AnimeStatus.PAUSED to "Paused",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .wrapContentHeight(),
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
    context: android.content.Context,
    anime: com.sulaiman.anilocal.domain.model.LocalAnime,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column {
            AsyncImage(
                model = ImageLoaderUtil.getPosterData(context, anime.id, anime.coverImage),
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
                    val statusEmoji = when (anime.status) {
                        AnimeStatus.WATCHING -> "▶️"
                        AnimeStatus.COMPLETED -> "✅"
                        AnimeStatus.PLANNING -> "📋"
                        AnimeStatus.DROPPED -> "❌"
                        AnimeStatus.PAUSED -> "⏸"
                    }
                    Text(
                        text = statusEmoji,
                        style = MaterialTheme.typography.labelSmall
                    )
                    if (anime.mediaStatus == "RELEASING" && anime.nextEpisode != null) {
                        Text(
                            text = "Ep ${anime.nextEpisode}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AniBlue
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
