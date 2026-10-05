package com.raulcatalinas.shopping.screens.settings.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcatalinas.shopping.screens.settings.enums.LanguageOptions
import com.raulcatalinas.shopping.screens.settings.enums.ThemeOptions
import com.raulcatalinas.shopping.screens.settings.repositories.SettingsRepository
import com.raulcatalinas.shopping.screens.settings.types.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    var settings by mutableStateOf(AppSettings())
        private set

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settings = AppSettings(
                theme = settingsRepository.getTheme(),
                language = settingsRepository.getLanguage()
            )
        }
    }

    fun setTheme(theme: ThemeOptions) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
            settings = settings.copy(theme = theme)
        }
    }

    fun setLanguage(language: LanguageOptions) {
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
            settings = settings.copy(language = language)
        }
    }
}