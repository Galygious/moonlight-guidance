package org.arcanaforge.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.arcanaforge.app.core.notifications.ScheduleNotificationHelper
import org.arcanaforge.app.ui.navigation.MoonlightGuidanceApp
import org.arcanaforge.app.ui.navigation.PreparedReadingRequest
import org.arcanaforge.app.ui.theme.MoonlightGuidanceTheme

class MainActivity : ComponentActivity() {
    private val preparedReadingRequest = mutableStateOf<PreparedReadingRequest?>(null)
    private val openSchedulesRequest = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleNotificationIntent(intent)

        val appContainer = (application as MoonlightGuidanceApplication).container
        setContent {
            val settings by appContainer.settingsRepository.settings.collectAsStateWithLifecycle(
                initialValue = org.arcanaforge.app.core.datastore.UserSettings(),
            )

            MoonlightGuidanceTheme(themeSetting = settings.themeSetting) {
                MoonlightGuidanceApp(
                    container = appContainer,
                    preparedReadingRequest = preparedReadingRequest.value,
                    openSchedulesRequest = openSchedulesRequest.value,
                    onExternalRequestHandled = {
                        preparedReadingRequest.value = null
                        openSchedulesRequest.value = false
                    },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent == null) return
        val deckId = intent.getStringExtra(ScheduleNotificationHelper.EXTRA_DECK_ID).orEmpty()
        val layoutId = intent.getStringExtra(ScheduleNotificationHelper.EXTRA_LAYOUT_ID).orEmpty()
        if (deckId.isNotBlank() && layoutId.isNotBlank()) {
            preparedReadingRequest.value = PreparedReadingRequest(
                deckId = deckId,
                layoutId = layoutId,
                question = intent.getStringExtra(ScheduleNotificationHelper.EXTRA_QUESTION).orEmpty(),
            )
            return
        }
        if (intent.getBooleanExtra(ScheduleNotificationHelper.EXTRA_OPEN_SCHEDULES, false)) {
            openSchedulesRequest.value = true
        }
    }
}
