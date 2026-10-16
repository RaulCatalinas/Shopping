package com.raulcatalinas.shopping.core.mappers

import com.raulcatalinas.shopping.backend.db.dto.ShoppingElementDto
import com.raulcatalinas.shopping.shared.types.ShoppingElement

fun ShoppingElementDto.toDomain() = ShoppingElement(
    id = id,
    name = name,
    quantity = quantity,
    listId = listId,
    isChecked = isChecked
)

fun List<ShoppingElementDto>.toDomain() = map { it.toDomain() }