package com.raulcatalinas.shopping.shared.extensions

import android.util.Patterns

fun CharSequence?.containsWhiteSpace(): Boolean {
    return !isNullOrBlank() && trim().any { it.isWhitespace() }
}

fun CharSequence?.isValidEmail(): Boolean {
    return !isNullOrBlank() && Patterns.EMAIL_ADDRESS.matcher(trim()).matches()
}

fun CharSequence.toEmoji(): String {
    val codePoint =
        removePrefix("U+")
            .toString()
            .toInt(16)

    return Character
        .toChars(codePoint)
        .concatToString()
}