package com.sulaiman.anilocal.presentation.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sulaiman.anilocal.domain.repository.AnimeRepository
import kotlinx.coroutines.delay

@Composable
fun AppHeader(
    title: String,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(100.dp)
) {
    val context = LocalContext.current
    var bannerUrl by remember { mutableStateOf<String?>(null) }
    var alpha by remember { mutableStateOf(1f) }

    // Collect library to get banners
    val animeRepository = remember {
        // We can't inject repo directly in a composable, so we use a simple approach
        null
    }

    // Use hardcoded popular anime banners as fallback (reliable AniList CDN)
    val fallbackBanners = remember {
        listOf(
            "https://s4.anilist.co/file/anilistcdn/media/anime/banner/21-YCDoAX13mPlM.jpg",
            "https://s4.anilist.co/file/anilistcdn/media/anime/banner/153510-OFuJzIGh.jpg",
            "https://s4.anilist.co/file/anilistcdn/media/anime/banner/16498-8jpFCOcDmneX.jpg",
            "https://s4.anilist.co/file/anilistcdn/media/anime/banner/11061-MhRsotLm.jpg",
            "https://s4.anilist.co/file/anilistcdn/media/anime/banner/20958-DlqbD0Vv.jpg",
            "https://s4.anilist.co/file/anilistcdn/media/anime/banner/113415.jpg",
            "https://s4.anilist.co/file/anilistcdn/media/anime/banner/21807.jpg",
            "https://s4.anilist.co/file/anilistcdn/media/anime/banner/104578.png",
            "https://s4.anilist.co/file/anilistcdn/media/anime/banner/21459.jpg",
            "https://s4.anilist.co/file/anilistcdn/media/anime/banner/97940.jpg",
        )
    }

    LaunchedEffect(Unit) {
        // Start with a random banner
        bannerUrl = fallbackBanners.random()
        while (true) {
            delay(14000)
            alpha = 0f
            delay(500)
            bannerUrl = fallbackBanners.random()
            delay(500)
            alpha = 1f
            delay(1000)
        }
    }

    Box(
        modifier = modifier
    ) {
        // Banner image
        bannerUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(alpha),
                contentScale = ContentScale.Crop
            )
        }

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color.Transparent,
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
        )

        // Title overlay
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White
            ),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )
    }
}
