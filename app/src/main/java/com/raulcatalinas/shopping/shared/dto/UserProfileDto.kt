package com.raulcatalinas.shopping.shared.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class UserProfileDto(
    val id: String,
    @SerialName("user_name")
    val userName: String,
    @SerialName("created_at")
    val createdAt: Instant? = null,
    @SerialName("updated_at")
    val updatedAt: Instant? = null
)