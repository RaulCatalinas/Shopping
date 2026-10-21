package com.raulcatalinas.shopping.core.lists.types

import com.raulcatalinas.shopping.backend.db.dto.ShoppingListDto

sealed interface ListChange {
    data object Connected : ListChange
    data class Upsert(val list: ShoppingListDto) : ListChange
    data class Deleted(val id: String) : ListChange
}