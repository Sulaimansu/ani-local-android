package com.sulaiman.anilocal.presentation.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sulaiman.anilocal.R
import com.sulaiman.anilocal.domain.model.LocalAnime
import com.sulaiman.anilocal.presentation.ui.theme.AniBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToDetail: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var query by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("🔍 Search AniList") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )

        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                viewModel.onQueryChange(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            label = { Text(stringResource(R.string.search_hint)) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AniBlue)
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            state.results.isEmpty() && query.length > 2 -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_results),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.results, key = { it.id }) { anime ->
                        SearchResultItem(
                            anime = anime,
                            isInLibrary = state.libraryIds.contains(anime.id),
                            onClick = { onNavigateToDetail(anime.id) },
                            onToggleLibrary = {
                                if (state.libraryIds.contains(anime.id)) {
                                    viewModel.removeFromLibrary(anime.id)
                                } else {
                                    viewModel.saveAnime(anime)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    anime: LocalAnime,
    isInLibrary: Boolean,
    onClick: () -> Unit,
    onToggleLibrary: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            AsyncImage(
                model = anime.coverImage,
                contentDescription = anime.titleRomaji,
                modifier = Modifier
                    .size(80.dp)
                    .aspectRatio(0.7f),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = anime.titleRomaji,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                anime.titleEnglish?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📺 ${anime.format.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AniBlue
                    )
                    anime.episodes?.let { eps ->
                        Text(
                            text = "📦 $eps eps",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                anime.genres.takeIf { it.isNotEmpty() }?.let { genres ->
                    Text(
                        text = genres.joinToString(" • ").take(50) + if (genres.joinToString(" • ").length > 50) "…" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isInLibrary) {
                    OutlinedButton(
                        onClick = onToggleLibrary,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("➖ Remove from Library", style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    FilledTonalButton(
                        onClick = onToggleLibrary,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("➕ Add to Library", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
