package com.raulcatalinas.shopping.shared.types

import kotlinx.serialization.Serializable

@Serializable
data class NewShoppingList(
    val name: String,
)