package com.sulaiman.anilocal.presentation.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sulaiman.anilocal.domain.model.AnimeFormat
import com.sulaiman.anilocal.domain.model.AnimeSeason
import com.sulaiman.anilocal.presentation.ui.theme.AniBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetailScreen(
    animeId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToRelated: (Int) -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(animeId) {
        viewModel.loadAnimeDetails(animeId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AniBlue)
                }
            }

            state.anime == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Failed to load anime details")
                }
            }

            else -> {
                DetailContent(
                    anime = state.anime!!,
                    isInLibrary = state.isInLibrary,
                    userStatus = state.userStatus,
                    countdownText = state.countdownText,
                    onSaveToLibrary = { viewModel.saveToLibrary() },
                    onRemoveFromLibrary = { viewModel.removeFromLibrary() },
                    onUpdateStatus = { viewModel.updateUserStatus(it) },
                    onNavigateToRelated = onNavigateToRelated,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        }
    }
}

@Composable
fun DetailContent(
    anime: com.sulaiman.anilocal.domain.model.LocalAnime,
    isInLibrary: Boolean,
    userStatus: com.sulaiman.anilocal.domain.model.AnimeStatus?,
    countdownText: String?,
    onSaveToLibrary: () -> Unit,
    onRemoveFromLibrary: () -> Unit,
    onUpdateStatus: (com.sulaiman.anilocal.domain.model.AnimeStatus) -> Unit,
    onNavigateToRelated: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.verticalScroll(scrollState)
    ) {
        // Banner
        anime.bannerImage?.let { banner ->
            AsyncImage(
                model = banner,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )
        }

        // Header
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = anime.coverImage,
                contentDescription = anime.titleRomaji,
                modifier = Modifier
                    .size(120.dp)
                    .aspectRatio(0.7f),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = anime.titleRomaji,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                anime.titleEnglish?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                anime.titleNative?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
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
                    anime.mediaStatus?.let { status ->
                        val statusEmoji = when (status) {
                            "RELEASING" -> "🔴"
                            "FINISHED" -> "✅"
                            "NOT_YET_RELEASED" -> "📅"
                            "CANCELLED" -> "❌"
                            "HIATUS" -> "⏸"
                            else -> ""
                        }
                        Text(
                            text = "$statusEmoji $status",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isInLibrary) {
                Button(
                    onClick = onSaveToLibrary,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("➕ Add to Library")
                }
            } else {
                OutlinedButton(
                    onClick = { /* Placeholder - remove button is below */ },
                    modifier = Modifier.weight(1f),
                    enabled = false
                ) {
                    Text("✓ In Library")
                }
            }
            anime.siteUrl?.let { url ->
                OutlinedButton(onClick = { /* Open URL - would need Activity context */ }) {
                    Text("🔗 AniList")
                }
            }
        }

        // Remove button (if in library)
        if (isInLibrary) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { onRemoveFromLibrary() }) {
                    Text("🗑 Remove from Library", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        // User status selector (if in library)
        if (isInLibrary) {
            UserStatusSelector(
                currentStatus = userStatus,
                onStatusChange = onUpdateStatus,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Countdown for airing episodes
        if (countdownText != null && anime.mediaStatus != "FINISHED") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📺", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Next Episode",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = countdownText,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Info chips
        InfoSection(
            anime = anime,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Studios
        if (anime.studios.isNotEmpty()) {
            SectionTitle("🎨 Studios")
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                anime.studios.forEach { studio ->
                    SuggestionChip(
                        onClick = { },
                        label = { Text(studio, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }

        // Genres
        if (anime.genres.isNotEmpty()) {
            SectionTitle("🏷 Genres")
            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                anime.genres.forEach { genre ->
                    SuggestionChip(
                        onClick = { },
                        label = { Text(genre, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }

        // Tags
        if (anime.tags.isNotEmpty()) {
            SectionTitle("🏷 Tags")
            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                anime.tags.take(10).forEach { tag ->
                    SuggestionChip(
                        onClick = { },
                        label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }

        // Description
        anime.description?.let { desc ->
            SectionTitle("📝 Description")
            Text(
                text = desc.replace(Regex("<[^>]*>"), ""),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Related anime
        anime.relationsJson?.let { json ->
            val relations = parseRelationsJson(json)
            if (relations.isNotEmpty()) {
                SectionTitle("🔗 Related Anime")
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    relations.forEach { relation ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onNavigateToRelated(relation.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AsyncImage(
                                    model = relation.coverImage,
                                    contentDescription = relation.titleRomaji,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .aspectRatio(0.7f),
                                    contentScale = ContentScale.Crop
                                )
                                Column {
                                    Text(
                                        text = relation.titleRomaji,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = relation.relationType,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AniBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun UserStatusSelector(
    currentStatus: com.sulaiman.anilocal.domain.model.AnimeStatus?,
    onStatusChange: (com.sulaiman.anilocal.domain.model.AnimeStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Your Status",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            com.sulaiman.anilocal.domain.model.AnimeStatus.values().forEach { status ->
                FilterChip(
                    selected = currentStatus == status,
                    onClick = { onStatusChange(status) },
                    label = { Text(status.name.take(1), style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun InfoSection(
    anime: com.sulaiman.anilocal.domain.model.LocalAnime,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            InfoItem("📦", anime.episodes?.toString() ?: "?", "Episodes")
            anime.duration?.let { InfoItem("⏱", "${it}m", "Duration") }
            anime.averageScore?.let { InfoItem("⭐", "${it}%", "Score") }
            anime.popularity?.let { InfoItem("👥", formatNumber(it), "Popularity") }
        }
    }
}

@Composable
fun InfoItem(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, style = MaterialTheme.typography.titleMedium)
        Text(value, style = MaterialTheme.typography.titleSmall)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier.horizontalScroll(scrollState),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.Top
    ) {
        content()
    }
}

fun formatNumber(num: Int): String = when {
    num >= 1_000_000 -> "${num / 1_000_000}M"
    num >= 1_000 -> "${num / 1_000}K"
    else -> num.toString()
}

fun parseRelationsJson(json: String): List<com.sulaiman.anilocal.domain.model.AnimeRelation> {
    return try {
        val regex = """\{"type":"([^"]*)","id":(\d+),"title":"([^"]*)","cover":"([^"]*)"(?:,"titleEn":"([^"]*)","status":"([^"]*)","format":"([^"]*)")?\}""".toRegex()
        regex.findAll(json).map { match ->
            val (type, id, title, cover, titleEn, status, format) = match.destructured
            com.sulaiman.anilocal.domain.model.AnimeRelation(
                id = id.toIntOrNull() ?: 0,
                titleRomaji = title,
                titleEnglish = titleEn.takeIf { it.isNotEmpty() },
                relationType = type,
                coverImage = cover.takeIf { it.isNotEmpty() },
                status = status.takeIf { it.isNotEmpty() },
                format = format.takeIf { it.isNotEmpty() }
            )
        }.toList()
    } catch (e: Exception) {
        emptyList()
    }
}
