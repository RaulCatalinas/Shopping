package com.raulcatalinas.shopping.backend.auth.utils

import com.raulcatalinas.shopping.screens.auth.enums.AuthMode

fun isAuthFormValid(
    authMode: AuthMode,
    userName: String,
    email: String,
    password: String
): Boolean {
    if (authMode == AuthMode.SIGN_UP) {
        return userName.isNotBlank() && email.isNotBlank() && password.isNotBlank()
    }

    return email.isNotBlank() && password.isNotBlank()
}