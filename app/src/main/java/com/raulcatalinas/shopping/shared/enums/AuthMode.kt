package com.raulcatalinas.shopping.shared.enums

import androidx.annotation.StringRes
import com.raulcatalinas.shopping.R

enum class AuthMode(@StringRes val labelRes: Int) {
    LOGIN(R.string.login),
    SIGN_UP(R.string.sign_up)
}