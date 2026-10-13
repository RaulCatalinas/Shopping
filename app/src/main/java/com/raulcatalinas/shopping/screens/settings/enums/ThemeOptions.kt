package com.raulcatalinas.shopping.screens.settings.enums

import androidx.annotation.StringRes
import com.raulcatalinas.shopping.R

enum class ThemeOptions(@StringRes val labelRes: Int) {
    Light(R.string.light_theme),
    Dark(R.string.dark_theme),
    System(R.string.system)
}