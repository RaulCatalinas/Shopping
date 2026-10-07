package com.raulcatalinas.shopping.shared.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ShoppingElementDto(
    val id: String,
    val name: String,
    val quantity: Int,
    @SerialName("list_id")
    val listId: String,
    @SerialName("is_checked")
    val isChecked: Boolean = false,
    @SerialName("created_at")
    val createdAt: Instant? = null,
    @SerialName("updated_at")
    val updatedAt: Instant? = null
)
