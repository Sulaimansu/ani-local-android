package com.sulaiman.anilocal.presentation.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sulaiman.anilocal.domain.model.LocalAnime

@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search AniList") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.onQueryChange(it)
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                label = { Text("Search...") },
                singleLine = true
            )

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally))
            } else {
                LazyColumn {
                    items(state.results) { anime ->
                        AnimeResultItem(anime) {
                            viewModel.saveAnime(anime)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimeResultItem(anime: LocalAnime, onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(anime.titleRomaji, style = MaterialTheme.typography.titleMedium)
                anime.titleEnglish?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
                Button(onClick = onAdd, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Add to Local")
                }
            }
        }
    }
}