package com.raulcatalinas.shopping.backend.auth.viewmodels

sealed interface AuthState {
    data object Loading : AuthState
    data object Authenticated : AuthState
    data object Unauthenticated : AuthState
}