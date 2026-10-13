package com.raulcatalinas.shopping.screens.settings.repositories

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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
    }

    private val settingsFlow = dataStore.data.map { preferences ->
        AppSettings(
            theme = preferences[Keys.THEME]?.let { value ->
                runCatching { ThemeOptions.valueOf(value) }.getOrNull()
            } ?: ThemeOptions.System
        )
    }

    fun getSettings() = settingsFlow

    suspend fun setTheme(theme: ThemeOptions) {
        dataStore.edit { preferences ->
            preferences[Keys.THEME] = theme.name
        }
    }

    fun setLanguage(language: LanguageOptions) {
        val locales = if (language == LanguageOptions.SYSTEM) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(language.code)
        }

        AppCompatDelegate.setApplicationLocales(locales)
    }

    // Reads the language that is really applied, even if the user changed it
    // from the system settings (Android 13+)
    fun getCurrentLanguage(): LanguageOptions {
        val locales = AppCompatDelegate.getApplicationLocales()

        if (locales.isEmpty) return LanguageOptions.SYSTEM

        val language = locales[0]?.language

        return LanguageOptions
            .entries
            .firstOrNull { it.code == language }
            ?: LanguageOptions.SYSTEM
    }
}