package com.raulcatalinas.shopping.screens.settings.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.raulcatalinas.shopping.screens.settings.constants.LANGUAGE_OPTION_KEY
import com.raulcatalinas.shopping.screens.settings.constants.THEME_OPTION_KEY
import com.raulcatalinas.shopping.screens.settings.enums.LanguageOptions
import com.raulcatalinas.shopping.screens.settings.enums.ThemeOptions
import com.raulcatalinas.shopping.screens.settings.types.AppSettings
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val THEME = stringPreferencesKey(THEME_OPTION_KEY)
        val LANGUAGE = stringPreferencesKey(LANGUAGE_OPTION_KEY)
    }

    private val settingsFlow = dataStore.data.map { preferences ->
        AppSettings(
            theme = preferences[Keys.THEME]?.let { value ->
                runCatching { ThemeOptions.valueOf(value) }.getOrNull()
            } ?: ThemeOptions.System,

            language = preferences[Keys.LANGUAGE]?.let { value ->
                runCatching { LanguageOptions.valueOf(value) }.getOrNull()
            } ?: LanguageOptions.SYSTEM
        )
    }

    fun getSettings() = settingsFlow

    suspend fun setTheme(theme: ThemeOptions) {
        dataStore.edit { preferences ->
            preferences[Keys.THEME] = theme.name
        }
    }

    suspend fun setLanguage(language: LanguageOptions) {
        dataStore.edit { preferences ->
            preferences[Keys.LANGUAGE] = language.name
        }
    }
}