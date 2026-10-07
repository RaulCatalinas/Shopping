package com.raulcatalinas.shopping.shared.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ShoppingListDto(
    val id: String,
    val name: String,
    @SerialName("owner_id")
    val ownerId: String,
    @SerialName("member_ids")
    val memberIds: List<String>,
    @SerialName("total_count")
    val totalCount: Int = 0,
    @SerialName("pending_count")
    val pendingCount: Int = 0,
    @SerialName("created_at")
    val createdAt: Instant? = null,
    @SerialName("updated_at")
    val updatedAt: Instant? = null
)