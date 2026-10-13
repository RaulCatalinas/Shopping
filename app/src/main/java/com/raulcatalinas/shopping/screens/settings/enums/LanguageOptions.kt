package com.raulcatalinas.shopping.screens.settings.enums

import androidx.annotation.StringRes
import com.raulcatalinas.shopping.R

enum class LanguageOptions(@StringRes val labelRes: Int) {
    ENGLISH(R.string.english),
    SPANISH(R.string.spanish),
    SYSTEM(R.string.system)
}