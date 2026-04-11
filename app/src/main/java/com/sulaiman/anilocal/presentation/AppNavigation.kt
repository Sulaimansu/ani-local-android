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

private val bottomNavRoutes = listOf(
    NavRoute("library", "Library", "📚"),
    NavRoute("search", "Search", "🔍"),
    NavRoute("airing", "Airing", "📺")
)

private data class NavRoute(val route: String, val title: String, val iconText: String)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != null && currentRoute in bottomNavRoutes.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavRoutes.forEach { route ->
                        NavigationBarItem(
                            icon = { Text(route.iconText, fontSize = 20.sp) },
                            label = { Text(route.title) },
                            selected = currentRoute == route.route,
                            onClick = {
                                navController.navigate(route.route) {
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
            startDestination = "library",
            modifier = Modifier.padding(padding)
        ) {
            composable("library") {
                LibraryScreen(
                    onNavigateToDetail = { animeId ->
                        navController.navigate("detail/$animeId")
                    }
                )
            }
            composable("search") {
                SearchScreen(
                    onNavigateToDetail = { animeId ->
                        navController.navigate("detail/$animeId")
                    }
                )
            }
            composable("airing") {
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
