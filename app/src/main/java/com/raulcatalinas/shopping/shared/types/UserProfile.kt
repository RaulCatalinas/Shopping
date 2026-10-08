package com.raulcatalinas.shopping.shared.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    @SerialName("username")
    val userName: String,
)