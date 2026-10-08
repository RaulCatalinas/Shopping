package com.raulcatalinas.shopping.backend.db.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckUsernameParams(
    @SerialName("p_username")
    val username: String
)