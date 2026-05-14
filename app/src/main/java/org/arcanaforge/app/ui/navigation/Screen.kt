package org.arcanaforge.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Interests
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Home : Screen("home", "Home", Icons.Outlined.Home)
    data object Decks : Screen("decks", "Decks", Icons.AutoMirrored.Outlined.LibraryBooks)
    data object Readings : Screen("readings", "Readings", Icons.Outlined.AutoStories)
    data object Layouts : Screen("layouts", "Layouts", Icons.Outlined.Interests)
    data object Schedule : Screen("schedule", "Schedule", Icons.Outlined.CalendarMonth)
    data object Settings : Screen("settings", "Settings", Icons.Outlined.Settings)
    data object ImportExport : Screen("import_export", "Import/Export", Icons.Outlined.AutoStories)

    companion object {
        val topLevel = listOf(Home, Decks, Readings, Layouts, Schedule)
    }
}
