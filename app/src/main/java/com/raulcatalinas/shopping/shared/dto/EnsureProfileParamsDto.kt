package com.raulcatalinas.shopping.shared.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class EnsureProfileParams(
    @SerialName("p_username")
    val username: String
)