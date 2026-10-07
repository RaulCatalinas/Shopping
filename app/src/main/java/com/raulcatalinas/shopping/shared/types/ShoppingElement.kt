package com.raulcatalinas.shopping.shared.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShoppingElement(
    val name: String,
    val quantity: Int,
    @SerialName("list_id")
    val listId: String,
    @SerialName("is_checked")
    val isChecked: Boolean = false,
)
