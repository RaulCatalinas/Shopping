package com.raulcatalinas.shopping.backend.auth.providers.dto

data class GoogleUserDto(
    val idToken: String,
    val name: String?
)