package org.arcanaforge.app.ui.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Interests
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Style
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
    data object DeckEditor : Screen("deck_editor/{deckId}", "Deck Editor", Icons.Outlined.Edit) {
        fun createRoute(deckId: String): String = "deck_editor/$deckId"
    }

    data object CardEditor : Screen("card_editor/{cardId}", "Card Editor", Icons.Outlined.Style) {
        fun createRoute(cardId: String): String = "card_editor/$cardId"
    }

    data object ReadingCreate : Screen(
        "reading_create?deckId={deckId}&layoutId={layoutId}&question={question}",
        "New Reading",
        Icons.Outlined.AutoStories,
    ) {
        const val baseRoute = "reading_create"

        fun createRoute(
            deckId: String = "",
            layoutId: String = "",
            question: String = "",
        ): String =
            "reading_create?deckId=${Uri.encode(deckId)}&layoutId=${Uri.encode(layoutId)}&question=${Uri.encode(question)}"
    }

    data object ReadingDetail : Screen("reading_detail/{readingId}", "Reading", Icons.Outlined.AutoStories) {
        fun createRoute(readingId: String): String = "reading_detail/$readingId"
    }

    data object LayoutEditor : Screen("layout_editor/{layoutId}", "Layout Editor", Icons.Outlined.Edit) {
        fun createRoute(layoutId: String): String = "layout_editor/$layoutId"
    }

    companion object {
        val topLevel: List<Screen>
            get() = listOf(Home, Decks, Readings, Layouts, Schedule)

        val all: List<Screen>
            get() = topLevel + Settings + ImportExport + DeckEditor + CardEditor + ReadingCreate + ReadingDetail + LayoutEditor
    }
}
