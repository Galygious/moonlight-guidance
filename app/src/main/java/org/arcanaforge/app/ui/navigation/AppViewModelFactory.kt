package org.arcanaforge.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.arcanaforge.app.AppContainer
import org.arcanaforge.app.ui.screens.decks.DeckLibraryViewModel
import org.arcanaforge.app.ui.screens.home.HomeViewModel

class AppViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(
                deckRepository = container.deckRepository,
                readingRepository = container.readingRepository,
                scheduleRepository = container.scheduleRepository,
            ) as T

            modelClass.isAssignableFrom(DeckLibraryViewModel::class.java) -> DeckLibraryViewModel(
                deckRepository = container.deckRepository,
            ) as T

            else -> error("Unknown ViewModel class: ${modelClass.name}")
        }
}
