package com.sulaiman.anilocal.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

// Curated list of popular anime banner images from AniList CDN
private val animeBanners = listOf(
    "https://s4.anilist.co/file/anilistcdn/media/anime/banner/21-YCDoAX13mPlM.jpg",
    "https://s4.anilist.co/file/anilistcdn/media/anime/banner/153510-OFuJzIGh.jpg",
    "https://s4.anilist.co/file/anilistcdn/media/anime/banner/16498-8jpFCOcDmneX.jpg",
    "https://s4.anilist.co/file/anilistcdn/media/anime/banner/11061-MhRsotLm.jpg",
    "https://s4.anilist.co/file/anilistcdn/media/anime/banner/20958-DlqbD0Vv.jpg",
    "https://s4.anilist.co/file/anilistcdn/media/anime/banner/101922-PEn1HcTz.jpg",
    "https://s4.anilist.co/file/anilistcdn/media/anime/banner/20605.png",
    "https://s4.anilist.co/file/anilistcdn/media/anime/banner/113415.jpg",
    "https://s4.anilist.co/file/anilistcdn/media/anime/banner/21807.jpg",
    "https://s4.anilist.co/file/anilistcdn/media/anime/banner/127230.jpg",
    "https://s4.anilist.co/file/anilistcdn/media/anime/banner/104578.png",
    "https://s4.anilist.co/file/anilistcdn/media/anime/banner/21459.jpg",
    "https://s4.anilist.co/file/anilistcdn/media/anime/banner/97940.jpg",
    "https://s4.anilist.co/file/anilistcdn/media/anime/banner/142838.jpg",
    "https://s4.anilist.co/file/anilistcdn/media/anime/banner/108632.jpg",
)

@Composable
fun RandomAnimeBanner(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(120.dp)
) {
    var currentBanner by remember { mutableStateOf(animeBanners.random()) }
    var alpha by remember { mutableStateOf(1f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(14000)
            alpha = 0f
            delay(500)
            currentBanner = animeBanners.random()
            delay(500)
            alpha = 1f
            delay(1000)
        }
    }

    AsyncImage(
        model = currentBanner,
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        alpha = alpha
    )
}
