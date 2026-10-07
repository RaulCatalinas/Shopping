package com.raulcatalinas.shopping.shared.utils

import android.util.Patterns

fun isValidEmail(email: String): Boolean {
    return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
}