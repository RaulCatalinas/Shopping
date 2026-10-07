package com.raulcatalinas.shopping.backend.db

import com.raulcatalinas.shopping.backend.db.constants.IdColumn
import com.raulcatalinas.shopping.backend.db.constants.ListIdColumn
import com.raulcatalinas.shopping.backend.db.constants.TABLE_SHOPPING_ELEMENTS
import com.raulcatalinas.shopping.backend.db.constants.TABLE_SHOPPING_LISTS
import com.raulcatalinas.shopping.backend.db.constants.TABLE_SHOPPING_PROFILES
import com.raulcatalinas.shopping.shared.dto.ShoppingElementDto
import com.raulcatalinas.shopping.shared.dto.ShoppingListDto
import com.raulcatalinas.shopping.shared.dto.UserProfileDto
import com.raulcatalinas.shopping.shared.types.ShoppingElement
import com.raulcatalinas.shopping.shared.types.ShoppingList
import com.raulcatalinas.shopping.shared.types.UserProfile
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject

class DbRepository @Inject constructor(
    private val db: Postgrest
) {
    suspend fun getAllList(): List<ShoppingListDto> {
        return db.from(TABLE_SHOPPING_LISTS)
            .select()
            .decodeList<ShoppingListDto>()
    }

    suspend fun getListById(id: String): ShoppingListDto? {
        return db.from(TABLE_SHOPPING_LISTS)
            .select {
                filter {
                    eq(IdColumn, id)
                }
            }
            .decodeSingleOrNull<ShoppingListDto>()
    }

    suspend fun createList(list: ShoppingList): ShoppingListDto? {
        return db.from(TABLE_SHOPPING_LISTS)
            .insert(list) {
                select()
            }
            .decodeSingleOrNull<ShoppingListDto>()
    }

    suspend fun createShoppingElement(
        listId: String,
        element: ShoppingElement
    ): ShoppingElementDto? {
        return db.from(TABLE_SHOPPING_ELEMENTS)
            .insert(element.copy(listId = listId)) {
                select()
            }
            .decodeSingleOrNull<ShoppingElementDto>()
    }

    suspend fun getShoppingElementsByListId(
        listId: String
    ): List<ShoppingElementDto> {
        return db.from(TABLE_SHOPPING_ELEMENTS)
            .select {
                filter {
                    eq(ListIdColumn, listId)
                }
            }
            .decodeList<ShoppingElementDto>()
    }

    suspend fun updateShoppingElement(
        listId: String,
        elementId: String,
        updatedElement: ShoppingElement
    ): ShoppingElementDto? {
        return db.from(TABLE_SHOPPING_ELEMENTS)
            .update(updatedElement) {
                filter {
                    eq(IdColumn, elementId)
                    eq(ListIdColumn, listId)
                }
                select()
            }
            .decodeSingleOrNull<ShoppingElementDto>()
    }

    suspend fun getUserProfile(userId: String): UserProfileDto? {
        return db.from(TABLE_SHOPPING_PROFILES)
            .select {
                filter {
                    eq(IdColumn, userId)
                }
            }
            .decodeSingleOrNull<UserProfileDto>()
    }

    suspend fun createUserProfile(profile: UserProfile): UserProfileDto? {
        return db.from(TABLE_SHOPPING_PROFILES)
            .insert(profile) {
                select()
            }
            .decodeSingleOrNull<UserProfileDto>()
    }

    suspend fun updateUserProfile(userId: String, updatedProfile: UserProfile): UserProfileDto? {
        return db.from(TABLE_SHOPPING_PROFILES)
            .update(updatedProfile) {
                filter {
                    eq(IdColumn, userId)
                }
                select()
            }
            .decodeSingleOrNull<UserProfileDto>()
    }
}