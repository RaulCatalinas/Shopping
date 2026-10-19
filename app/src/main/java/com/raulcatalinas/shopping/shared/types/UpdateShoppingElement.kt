package com.raulcatalinas.shopping.shared.types

import kotlinx.serialization.Serializable

@Serializable
data class UpdateShoppingElement(
    val name: String,
    val quantity: Int
)
