package org.arcanaforge.app.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsRepository(
    private val context: Context,
) {
    val settings: Flow<UserSettings> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserSettings(
                themeSetting = preferences[Keys.Theme]?.let(::themeFromStorage) ?: ThemeSetting.System,
                defaultReversalsEnabled = preferences[Keys.DefaultReversals] ?: true,
                defaultRevealMode = preferences[Keys.DefaultRevealMode] ?: "one_by_one",
            )
        }

    suspend fun setThemeSetting(themeSetting: ThemeSetting) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.Theme] = themeSetting.name
        }
    }

    private fun themeFromStorage(value: String): ThemeSetting =
        ThemeSetting.entries.firstOrNull { it.name == value } ?: ThemeSetting.System

    private fun emptyPreferences(): Preferences = androidx.datastore.preferences.core.emptyPreferences()

    private object Keys {
        val Theme = stringPreferencesKey("theme")
        val DefaultReversals = booleanPreferencesKey("default_reversals")
        val DefaultRevealMode = stringPreferencesKey("default_reveal_mode")
    }
}
