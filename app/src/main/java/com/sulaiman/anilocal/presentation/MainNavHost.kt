package com.sulaiman.anilocal.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sulaiman.anilocal.presentation.screens.library.LibraryScreen
import com.sulaiman.anilocal.presentation.screens.search.SearchScreen

@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "library") {
        composable("library") {
            LibraryScreen(onNavigateToSearch = { navController.navigate("search") })
        }
        composable("search") {
            SearchScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}