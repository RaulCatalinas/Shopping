package com.raulcatalinas.shopping.core.lists.repositories

import android.util.Log
import com.raulcatalinas.shopping.backend.db.ElementsDataSource
import com.raulcatalinas.shopping.backend.db.ListsDataSource
import com.raulcatalinas.shopping.backend.db.dto.ShoppingElementDto
import com.raulcatalinas.shopping.backend.db.dto.ShoppingListDto
import com.raulcatalinas.shopping.core.lists.repositories.constants.LISTS_REPOSITORY_TAG
import com.raulcatalinas.shopping.core.lists.types.ElementChange
import com.raulcatalinas.shopping.core.lists.types.ListChange
import com.raulcatalinas.shopping.core.mappers.toDomain
import com.raulcatalinas.shopping.shared.types.NewShoppingElement
import com.raulcatalinas.shopping.shared.types.NewShoppingList
import com.raulcatalinas.shopping.shared.types.ShoppingElement
import com.raulcatalinas.shopping.shared.types.ShoppingList
import com.raulcatalinas.shopping.shared.types.UpdateShoppingElement
import com.raulcatalinas.shopping.shared.types.UpdateShoppingList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ListsRepository @Inject constructor(
    private val listsDataSource: ListsDataSource,
    private val elementsDataSource: ElementsDataSource
) {
    suspend fun getAllList(): Result<List<ShoppingListDto>> {
        return try {
            Result.success(listsDataSource.getAllList())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(LISTS_REPOSITORY_TAG, "Error getting user lists: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getListById(id: String): Result<ShoppingListDto?> {
        return try {
            Result.success(listsDataSource.getListById(id))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(LISTS_REPOSITORY_TAG, "Error getting user list with id $id: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getElementsByListId(listId: String): Result<List<ShoppingElementDto>> {
        return try {
            Result.success(elementsDataSource.getShoppingElementsByListId(listId))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(LISTS_REPOSITORY_TAG, "Error getting elements of list $listId: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun createList(listName: String): ShoppingList? {
        val createdList =
            listsDataSource.createList(
                NewShoppingList(listName.trim())
            )

        return createdList?.toDomain()
    }

    suspend fun createShoppingElement(
        elementName: String,
        quantity: Int,
        listId: String
    ): ShoppingElement? {
        val shoppingElement = NewShoppingElement(
            name = elementName.trim(),
            quantity = quantity,
            listId = listId
        )

        val createdElement = elementsDataSource.createShoppingElement(shoppingElement)

        return createdElement?.toDomain()
    }

    suspend fun deleteList(id: String): Result<Unit> {
        return try {
            if (!listsDataSource.deleteList(id)) {
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

    suspend fun updateList(id: String, name: String): ShoppingList? {
        return listsDataSource
            .updateList(id, UpdateShoppingList(name.trim()))
            ?.toDomain()
    }

    suspend fun deleteShoppingElement(id: String): Result<Unit> {
        return try {
            if (!elementsDataSource.deleteShoppingElement(id)) {
                return Result.failure(IllegalStateException("The element was not deleted"))
            }

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(LISTS_REPOSITORY_TAG, "Error deleting element $id: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateShoppingElement(
        elementId: String,
        elementName: String,
        quantity: Int
    ): ShoppingElement? {
        val updated = elementsDataSource.updateShoppingElement(
            elementId = elementId,
            changes = UpdateShoppingElement(name = elementName.trim(), quantity = quantity)
        )

        return updated?.toDomain()
    }

    suspend fun setElementChecked(id: String, checked: Boolean): Result<Unit> {
        return try {
            if (!elementsDataSource.setElementChecked(id, checked)) {
                return Result.failure(IllegalStateException("The element was not updated"))
            }

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(LISTS_REPOSITORY_TAG, "Error updating element $id: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun observeElementChanges(listId: String): Flow<ElementChange> {
        return elementsDataSource
            .observeElementChanges(listId)
    }

    fun observeListChanges(): Flow<ListChange> {
        return listsDataSource.observeListChanges()
    }
}