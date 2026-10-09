package com.raulcatalinas.shopping.shared.extensions

import android.util.Patterns

fun CharSequence?.containsWhiteSpace(): Boolean {
    return !isNullOrBlank() && trim().any { it.isWhitespace() }
}

fun CharSequence?.isValidEmail(): Boolean {
    return !isNullOrBlank() && Patterns.EMAIL_ADDRESS.matcher(trim()).matches()
}