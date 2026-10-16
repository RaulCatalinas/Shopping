package com.raulcatalinas.shopping.core.lists.repositories

import android.util.Log
import com.raulcatalinas.shopping.backend.db.DbRepository
import com.raulcatalinas.shopping.backend.db.dto.ShoppingElementDto
import com.raulcatalinas.shopping.backend.db.dto.ShoppingListDto
import com.raulcatalinas.shopping.core.lists.repositories.constants.LISTS_REPOSITORY_TAG
import java.util.concurrent.CancellationException
import javax.inject.Inject

class ListsRepository @Inject constructor(
    private val dbRepository: DbRepository
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
}