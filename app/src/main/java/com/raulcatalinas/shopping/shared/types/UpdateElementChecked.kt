package com.raulcatalinas.shopping.shared.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateElementChecked(
    @SerialName("is_checked")
    val isChecked: Boolean
)
