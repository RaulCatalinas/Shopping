package com.raulcatalinas.shopping.backend.db.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateInvitationParams(
    @SerialName("p_list_id")
    val listId: String,
    @SerialName("p_valid_hours")
    val validHours: Int
)