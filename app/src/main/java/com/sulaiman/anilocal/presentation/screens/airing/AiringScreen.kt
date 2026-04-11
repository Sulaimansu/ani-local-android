package com.sulaiman.anilocal.presentation.screens.airing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.sulaiman.anilocal.util.ImageLoaderUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiringScreen(
    onNavigateToDetail: (Int) -> Unit,
    viewModel: AiringViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadAiringToday()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("📺 Airing Today") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AniBlue)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.loading),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = { viewModel.loadAiringToday() }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }

            state.airingAnime.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_airing_today),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.airingAnime, key = { "${it.id}-${it.episode}" }) { anime ->
                        AiringAnimeItem(
                            context = context,
                            anime = anime,
                            onClick = { onNavigateToDetail(anime.id) },
                            timeRemaining = anime.timeUntilAiring
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiringAnimeItem(
    context: android.content.Context,
    anime: com.sulaiman.anilocal.domain.model.AiringAnime,
    onClick: () -> Unit,
    timeRemaining: Int
) {
    val countdownText = remember(timeRemaining) {
        formatCountdown(timeRemaining)
    }

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
                model = ImageLoaderUtil.getPosterData(context, anime.id, anime.coverImage),
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
                    maxLines = 1,
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
                        text = "📺 Ep ${anime.episode}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AniBlue
                    )
                    Text(
                        text = "⏰ $countdownText",
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            timeRemaining < 0 -> MaterialTheme.colorScheme.error
                            timeRemaining < 600 -> MaterialTheme.colorScheme.error
                            timeRemaining < 3600 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                anime.studios.takeIf { it.isNotEmpty() }?.let { studios ->
                    Text(
                        text = "🎨 ${studios.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

fun formatCountdown(seconds: Int): String {
    val absSeconds = kotlin.math.abs(seconds)
    val d = absSeconds / 86400
    val h = (absSeconds % 86400) / 3600
    val m = (absSeconds % 3600) / 60
    val s = absSeconds % 60
    val prefix = if (seconds < 0) "Aired " else "In "
    return if (d > 0) "$prefix${d}d ${h}h"
    else if (h > 0) "$prefix${h}h ${m}m"
    else if (m > 0) "$prefix${m}m ${s}s"
    else "$prefix${s}s"
}
