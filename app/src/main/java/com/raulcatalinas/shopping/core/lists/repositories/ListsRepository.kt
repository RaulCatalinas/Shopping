package com.raulcatalinas.shopping.core.lists.repositories

import android.util.Log
import com.raulcatalinas.shopping.backend.auth.repositories.AuthRepository
import com.raulcatalinas.shopping.backend.db.DbRepository
import com.raulcatalinas.shopping.backend.db.dto.ShoppingElementDto
import com.raulcatalinas.shopping.backend.db.dto.ShoppingListDto
import com.raulcatalinas.shopping.core.lists.repositories.constants.LISTS_REPOSITORY_TAG
import com.raulcatalinas.shopping.core.mappers.toDomain
import com.raulcatalinas.shopping.shared.types.NewShoppingElement
import com.raulcatalinas.shopping.shared.types.NewShoppingList
import com.raulcatalinas.shopping.shared.types.ShoppingElement
import com.raulcatalinas.shopping.shared.types.ShoppingList
import java.util.concurrent.CancellationException
import javax.inject.Inject

class ListsRepository @Inject constructor(
    private val dbRepository: DbRepository,
    private val authRepository: AuthRepository
) {
    suspend fun getAllList(): Result<List<ShoppingListDto>> {
        try {
            val lists = dbRepository.getAllList()

            return Result.success(lists)
        } catch (e: Exception) {
            Log.e(
                LISTS_REPOSITORY_TAG,
                "Error getting user lists: ${e.message}",
                e
            )

            return Result.failure(e)
        }
    }

    suspend fun getListById(id: String): Result<ShoppingListDto?> {
        try {
            val list = dbRepository.getListById(id)

            return Result.success(list)
        } catch (e: Exception) {
            Log.e(
                LISTS_REPOSITORY_TAG,
                "Error getting user list with id $id: ${e.message}",
                e
            )

            return Result.failure(e)
        }
    }

    suspend fun getElementsByListId(listId: String): Result<List<ShoppingElementDto>> {
        return try {
            Result.success(dbRepository.getShoppingElementsByListId(listId))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(LISTS_REPOSITORY_TAG, "Error getting elements of list $listId: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun createList(listName: String): ShoppingList? {
        val currentUserId =
            authRepository
                .getCurrentUser()
                ?.id
                ?: throw IllegalStateException("User ID was not returned")

        val list = NewShoppingList(listName)

        val createdList = dbRepository.createList(list)

        return createdList?.toDomain()
    }

    suspend fun createShoppingElement(
        elementName: String,
        quantity: Int,
        listId: String
    ): ShoppingElement? {
        val shoppingElement = NewShoppingElement(
            name = elementName,
            quantity = quantity,
            listId = listId
        )

        val createdElement = dbRepository.createShoppingElement(shoppingElement)

        return createdElement?.toDomain()
    }

    suspend fun deleteList(id: String): Result<Unit> {
        return try {
            if (!dbRepository.deleteList(id)) {
                return Result.failure(IllegalStateException("The list was not deleted"))
            }

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(LISTS_REPOSITORY_TAG, "Error deleting list $id: ${e.message}", e)
            Result.failure(e)
        }
    }
}