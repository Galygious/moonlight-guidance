package org.arcanaforge.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.arcanaforge.app.AppContainer
import org.arcanaforge.app.ui.screens.decks.DeckLibraryScreen
import org.arcanaforge.app.ui.screens.home.HomeScreen
import org.arcanaforge.app.ui.screens.placeholder.PlaceholderScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoonlightGuidanceApp(container: AppContainer) {
    val navController = rememberNavController()
    val viewModelFactory = remember(container) { AppViewModelFactory(container) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route
    val currentScreen = (Screen.topLevel + Screen.Settings + Screen.ImportExport)
        .firstOrNull { it.route == currentRoute } ?: Screen.Home

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentScreen.label) },
                actions = {
                    IconButton(
                        onClick = { navController.navigateSingleTop(Screen.Settings.route) },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Open settings",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        },
        bottomBar = {
            NavigationBar {
                Screen.topLevel.forEach { screen ->
                    val selected = navBackStackEntry?.destination?.hierarchy
                        ?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = { navController.navigateSingleTop(screen.route) },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.label,
                            )
                        },
                        label = { Text(screen.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModelFactory = viewModelFactory,
                    onNavigate = navController::navigateSingleTop,
                )
            }
            composable(Screen.Decks.route) {
                DeckLibraryScreen(viewModelFactory = viewModelFactory)
            }
            composable(Screen.Readings.route) {
                PlaceholderScreen(
                    title = "Reading History",
                    body = "Reading creation and history arrive in Phase 4.",
                )
            }
            composable(Screen.Layouts.route) {
                PlaceholderScreen(
                    title = "Layout Library",
                    body = "Built-in layouts are seeded now. The library and canvas editor arrive in later phases.",
                )
            }
            composable(Screen.Schedule.route) {
                PlaceholderScreen(
                    title = "Scheduled Readings",
                    body = "Reminder scheduling arrives in Phase 7.",
                )
            }
            composable(Screen.Settings.route) {
                PlaceholderScreen(
                    title = "Settings",
                    body = "Theme, BYOK AI, privacy, and import/export settings arrive in later phases.",
                )
            }
            composable(Screen.ImportExport.route) {
                PlaceholderScreen(
                    title = "Import / Export",
                    body = "Deck and backup import/export arrive in Phase 9.",
                )
            }
        }
    }
}

private fun androidx.navigation.NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
