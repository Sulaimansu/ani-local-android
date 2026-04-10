package com.sulaiman.anilocal.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sulaiman.anilocal.presentation.screens.airing.AiringScreen
import com.sulaiman.anilocal.presentation.screens.library.LibraryScreen
import com.sulaiman.anilocal.presentation.screens.search.SearchScreen
import com.sulaiman.anilocal.presentation.screens.detail.AnimeDetailScreen

sealed class Screen(
    val route: String,
    val title: String,
    val iconText: String
) {
    data object Library : Screen("library", "Library", "📚")
    data object Search : Screen("search", "Search", "🔍")
    data object Airing : Screen("airing", "Airing", "📺")
    data object Detail : Screen("detail/{animeId}", "Details", "ℹ️")

    companion object {
        val bottomNavScreens = listOf(Library, Search, Airing)
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Library.route,
        Screen.Search.route,
        Screen.Airing.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    Screen.bottomNavScreens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Text(screen.iconText, fontSize = 20.sp) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Library.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Library.route) {
                LibraryScreen(
                    onNavigateToDetail = { animeId ->
                        navController.navigate("detail/$animeId")
                    }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    onNavigateToDetail = { animeId ->
                        navController.navigate("detail/$animeId")
                    }
                )
            }
            composable(Screen.Airing.route) {
                AiringScreen(
                    onNavigateToDetail = { animeId ->
                        navController.navigate("detail/$animeId")
                    }
                )
            }
            composable("detail/{animeId}") { backStackEntry ->
                val animeId = backStackEntry.arguments?.getString("animeId")?.toIntOrNull() ?: 0
                AnimeDetailScreen(
                    animeId = animeId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToRelated = { relatedId ->
                        navController.navigate("detail/$relatedId")
                    }
                )
            }
        }
    }
}
