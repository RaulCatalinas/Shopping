package com.raulcatalinas.shopping.shared.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShoppingList(
    val name: String,
    @SerialName("owner_id")
    val ownerId: String,
    @SerialName("member_ids")
    val memberIds: List<String>,
    @SerialName("total_count")
    val totalCount: Int = 0,
    @SerialName("pending_count")
    val pendingCount: Int = 0,
)