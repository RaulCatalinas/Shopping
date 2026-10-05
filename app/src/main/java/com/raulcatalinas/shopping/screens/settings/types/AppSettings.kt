package com.raulcatalinas.shopping.screens.settings.types

import com.raulcatalinas.shopping.screens.settings.enums.LanguageOptions
import com.raulcatalinas.shopping.screens.settings.enums.ThemeOptions

data class AppSettings(
    val theme: ThemeOptions = ThemeOptions.System,
    val language: LanguageOptions = LanguageOptions.ENGLISH
)
