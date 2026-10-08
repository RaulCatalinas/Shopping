package com.raulcatalinas.shopping.backend.auth.types

sealed interface UsernameState {
    data object Idle : UsernameState
    data object Checking : UsernameState
    data class Available(val userName: String) : UsernameState
    data class Taken(val userName: String) : UsernameState
    data class Error(val message: String) : UsernameState
}