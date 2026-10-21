package com.raulcatalinas.shopping.core.lists.types

import com.raulcatalinas.shopping.backend.db.dto.ShoppingElementDto

sealed interface ElementChange {
    data object Connected : ElementChange
    data class Upsert(val element: ShoppingElementDto) : ElementChange
    data class Deleted(val id: String) : ElementChange
}