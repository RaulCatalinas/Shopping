package com.raulcatalinas.shopping.shared.extensions

fun String.containsWhiteSpace(): Boolean {
    return this
        .trim()
        .any { it.isWhitespace() }
}