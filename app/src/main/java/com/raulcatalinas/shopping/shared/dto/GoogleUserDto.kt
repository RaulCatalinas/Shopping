package com.raulcatalinas.shopping.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class GoogleUserDto(
    val idToken: String,
    val email: String,
    val name: String?
)