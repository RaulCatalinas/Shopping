package com.raulcatalinas.shopping.shared.types

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val username: String,
)