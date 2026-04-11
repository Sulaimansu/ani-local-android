package com.sulaiman.anilocal.presentation.screens.releasing

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
import com.sulaiman.anilocal.util.ImageLoaderUtil
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleasingScreen(
    onNavigateToDetail: (Int) -> Unit,
    viewModel: ReleasingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(modifier = Modifier.fillMaxSize()) {

        if (state.anime.isEmpty() && !state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No releasing anime in your library",
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
                items(state.anime, key = { it.id }) { anime ->
                    ReleasingGridItem(
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
fun ReleasingGridItem(
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

                val timeText = remember(anime.nextAiringTime) {
                    val time = anime.nextAiringTime
                    if (time == null || time <= 0L) "No schedule"
                    else {
                        val remaining = time - System.currentTimeMillis()
                        if (remaining <= 0) "Airing now!"
                        else {
                            val d = TimeUnit.MILLISECONDS.toDays(remaining)
                            val h = TimeUnit.MILLISECONDS.toHours(remaining) % 24
                            val m = TimeUnit.MILLISECONDS.toMinutes(remaining) % 60
                            if (d > 0) "${d}d ${h}h"
                            else if (h > 0) "${h}h ${m}m"
                            else "${m}m"
                        }
                    }
                }

                Text(
                    text = if (anime.nextEpisode != null) "Ep ${anime.nextEpisode} in $timeText" else "Schedule: $timeText",
                    style = MaterialTheme.typography.labelSmall,
                    color = AniBlue
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = anime.mediaStatus ?: "Unknown",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
