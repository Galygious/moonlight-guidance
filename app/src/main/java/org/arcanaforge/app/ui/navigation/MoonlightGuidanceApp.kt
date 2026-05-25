package org.arcanaforge.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.arcanaforge.app.AppContainer
import org.arcanaforge.app.ui.screens.cardeditor.CardEditorScreen
import org.arcanaforge.app.ui.screens.deckeditor.DeckEditorScreen
import org.arcanaforge.app.ui.screens.decks.DeckLibraryScreen
import org.arcanaforge.app.ui.screens.home.HomeScreen
import org.arcanaforge.app.ui.screens.layouteditor.LayoutEditorScreen
import org.arcanaforge.app.ui.screens.layoutlibrary.LayoutLibraryScreen
import org.arcanaforge.app.ui.screens.placeholder.PlaceholderScreen
import org.arcanaforge.app.ui.screens.readings.ReadingCreateScreen
import org.arcanaforge.app.ui.screens.readings.ReadingDetailScreen
import org.arcanaforge.app.ui.screens.readings.ReadingHistoryScreen
import org.arcanaforge.app.ui.screens.schedule.ScheduleScreen
import org.arcanaforge.app.ui.screens.settings.SettingsScreen

data class PreparedReadingRequest(
    val deckId: String,
    val layoutId: String,
    val question: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoonlightGuidanceApp(
    container: AppContainer,
    preparedReadingRequest: PreparedReadingRequest? = null,
    openSchedulesRequest: Boolean = false,
    onExternalRequestHandled: () -> Unit = {},
) {
    val navController = rememberNavController()
    val viewModelFactory = remember(container) { AppViewModelFactory(container) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route
    val currentScreen = Screen.all.firstOrNull { it.route == currentRoute } ?: Screen.Home
    val canNavigateBack = Screen.topLevel.none { it.route == currentRoute }

    LaunchedEffect(preparedReadingRequest, openSchedulesRequest) {
        when {
            preparedReadingRequest != null -> {
                navController.navigate(
                    Screen.ReadingCreate.createRoute(
                        deckId = preparedReadingRequest.deckId,
                        layoutId = preparedReadingRequest.layoutId,
                        question = preparedReadingRequest.question,
                    ),
                )
                onExternalRequestHandled()
            }

            openSchedulesRequest -> {
                navController.navigateSingleTop(Screen.Schedule.route)
                onExternalRequestHandled()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentScreen.label) },
                navigationIcon = {
                    if (canNavigateBack) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Go back",
                            )
                        }
                    }
                },
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
                DeckLibraryScreen(
                    viewModelFactory = viewModelFactory,
                    onOpenDeck = { deckId ->
                        navController.navigate(Screen.DeckEditor.createRoute(deckId))
                    },
                )
            }
            composable(
                route = Screen.DeckEditor.route,
                arguments = listOf(navArgument("deckId") { type = NavType.StringType }),
            ) {
                DeckEditorScreen(
                    viewModelFactory = viewModelFactory,
                    onOpenCard = { cardId ->
                        navController.navigate(Screen.CardEditor.createRoute(cardId))
                    },
                    onDeleted = {
                        navController.popBackStack()
                    },
                )
            }
            composable(
                route = Screen.CardEditor.route,
                arguments = listOf(navArgument("cardId") { type = NavType.StringType }),
            ) {
                CardEditorScreen(viewModelFactory = viewModelFactory)
            }
            composable(Screen.Readings.route) {
                ReadingHistoryScreen(
                    viewModelFactory = viewModelFactory,
                    onCreateReading = {
                        navController.navigate(Screen.ReadingCreate.createRoute())
                    },
                    onOpenReading = { readingId ->
                        navController.navigate(Screen.ReadingDetail.createRoute(readingId))
                    },
                )
            }
            composable(
                route = Screen.ReadingCreate.route,
                arguments = listOf(
                    navArgument("deckId") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("layoutId") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("question") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
            ) {
                ReadingCreateScreen(
                    viewModelFactory = viewModelFactory,
                    onReadingCreated = { readingId ->
                        navController.navigate(Screen.ReadingDetail.createRoute(readingId)) {
                            popUpTo(Screen.Readings.route)
                        }
                    },
                )
            }
            composable(
                route = Screen.ReadingDetail.route,
                arguments = listOf(navArgument("readingId") { type = NavType.StringType }),
            ) {
                ReadingDetailScreen(
                    viewModelFactory = viewModelFactory,
                    onDeleted = {
                        navController.popBackStack(Screen.Readings.route, inclusive = false)
                    },
                )
            }
            composable(Screen.Layouts.route) {
                LayoutLibraryScreen(
                    viewModelFactory = viewModelFactory,
                    onOpenLayout = { layoutId ->
                        navController.navigate(Screen.LayoutEditor.createRoute(layoutId))
                    },
                )
            }
            composable(
                route = Screen.LayoutEditor.route,
                arguments = listOf(navArgument("layoutId") { type = NavType.StringType }),
            ) {
                LayoutEditorScreen(
                    viewModelFactory = viewModelFactory,
                    onDeleted = {
                        navController.popBackStack(Screen.Layouts.route, inclusive = false)
                    },
                )
            }
            composable(Screen.Schedule.route) {
                ScheduleScreen(viewModelFactory = viewModelFactory)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModelFactory = viewModelFactory)
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
