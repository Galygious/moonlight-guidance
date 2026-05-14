package org.arcanaforge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.arcanaforge.app.ui.navigation.MoonlightGuidanceApp
import org.arcanaforge.app.ui.theme.MoonlightGuidanceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as MoonlightGuidanceApplication).container
        setContent {
            val settings by appContainer.settingsRepository.settings.collectAsStateWithLifecycle(
                initialValue = org.arcanaforge.app.core.datastore.UserSettings(),
            )

            MoonlightGuidanceTheme(themeSetting = settings.themeSetting) {
                MoonlightGuidanceApp(container = appContainer)
            }
        }
    }
}
