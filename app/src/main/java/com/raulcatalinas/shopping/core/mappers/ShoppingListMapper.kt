package com.raulcatalinas.shopping.core.mappers

import com.raulcatalinas.shopping.backend.db.dto.ShoppingListDto
import com.raulcatalinas.shopping.shared.types.ShoppingList

fun ShoppingListDto.toDomain(): ShoppingList {
    return ShoppingList(
        id = id,
        name = name,
        ownerId = ownerId,
        memberIds = memberIds,
        totalCount = totalCount,
        pendingCount = pendingCount
    )
}

fun List<ShoppingListDto>.toDomain(): List<ShoppingList> {
    return map { it.toDomain() }
}