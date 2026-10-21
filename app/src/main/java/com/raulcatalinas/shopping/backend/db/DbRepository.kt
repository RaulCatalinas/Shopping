package com.raulcatalinas.shopping.backend.db

import android.util.Log
import com.raulcatalinas.shopping.backend.db.constants.CHECK_USERNAME_EXISTS_RPC
import com.raulcatalinas.shopping.backend.db.constants.DB_REPOSITORY_TAG
import com.raulcatalinas.shopping.backend.db.constants.DELETE_USER_RPC
import com.raulcatalinas.shopping.backend.db.constants.ENSURE_USER_PROFILE_RPC
import com.raulcatalinas.shopping.backend.db.constants.IdColumn
import com.raulcatalinas.shopping.backend.db.constants.ListIdColumn
import com.raulcatalinas.shopping.backend.db.constants.TABLE_SHOPPING_ELEMENTS
import com.raulcatalinas.shopping.backend.db.constants.TABLE_SHOPPING_LISTS
import com.raulcatalinas.shopping.backend.db.constants.TABLE_SHOPPING_PROFILES
import com.raulcatalinas.shopping.backend.db.dto.CheckUsernameParams
import com.raulcatalinas.shopping.backend.db.dto.EnsureProfileParams
import com.raulcatalinas.shopping.backend.db.dto.ShoppingElementDto
import com.raulcatalinas.shopping.backend.db.dto.ShoppingListDto
import com.raulcatalinas.shopping.backend.profiles.dto.UserProfileDto
import com.raulcatalinas.shopping.core.lists.types.ElementChange
import com.raulcatalinas.shopping.core.lists.types.ListChange
import com.raulcatalinas.shopping.shared.types.NewShoppingElement
import com.raulcatalinas.shopping.shared.types.NewShoppingList
import com.raulcatalinas.shopping.shared.types.UpdateElementChecked
import com.raulcatalinas.shopping.shared.types.UpdateShoppingElement
import com.raulcatalinas.shopping.shared.types.UpdateShoppingList
import com.raulcatalinas.shopping.shared.types.UserProfile
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID
import javax.inject.Inject

class DbRepository @Inject constructor(
    private val db: Postgrest,
    private val realtime: Realtime
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

    suspend fun createList(list: NewShoppingList): ShoppingListDto? {
        return db.from(TABLE_SHOPPING_LISTS)
            .insert(list) {
                select()
            }
            .decodeSingleOrNull<ShoppingListDto>()
    }

    suspend fun deleteList(id: String): Boolean {
        val deleted = db.from(TABLE_SHOPPING_LISTS)
            .delete {
                select()
                filter { eq(IdColumn, id) }
            }
            .decodeList<ShoppingListDto>()

        return deleted.isNotEmpty()
    }

    suspend fun updateList(id: String, changes: UpdateShoppingList): ShoppingListDto? {
        return db.from(TABLE_SHOPPING_LISTS)
            .update(changes) {
                select()
                filter { eq(IdColumn, id) }
            }
            .decodeSingleOrNull<ShoppingListDto>()
    }

    suspend fun createShoppingElement(element: NewShoppingElement): ShoppingElementDto? {
        return db.from(TABLE_SHOPPING_ELEMENTS)
            .insert(element) {
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
        elementId: String,
        changes: UpdateShoppingElement
    ): ShoppingElementDto? {
        return db.from(TABLE_SHOPPING_ELEMENTS)
            .update(changes) {
                select()
                filter { eq(IdColumn, elementId) }
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

    suspend fun deleteAccount(userId: String) {
        db.from(TABLE_SHOPPING_PROFILES)
            .delete {
                filter {
                    eq(IdColumn, userId)
                }
            }

        db.rpc(DELETE_USER_RPC)
    }

    suspend fun checkUserNameExists(userName: String): Result<Boolean> {
        return try {
            val exists =
                db
                    .rpc(
                        function = CHECK_USERNAME_EXISTS_RPC,
                        parameters = CheckUsernameParams(
                            username = userName.trim()
                        )
                    )
                    .decodeAs<Boolean>()

            Result.success(exists)
        } catch (e: Exception) {
            Log.e(
                DB_REPOSITORY_TAG,
                "Error checking username: ${e.message}",
                e
            )
            Result.failure(e)
        }
    }

    suspend fun ensureUserProfile(username: String): Result<Unit> {
        return try {
            db.rpc(
                function = ENSURE_USER_PROFILE_RPC,
                parameters = EnsureProfileParams(username = username.trim())
            )

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(DB_REPOSITORY_TAG, "Error ensuring profile: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteShoppingElement(id: String): Boolean {
        val deleted = db.from(TABLE_SHOPPING_ELEMENTS)
            .delete {
                select()
                filter { eq(IdColumn, id) }
            }
            .decodeList<ShoppingElementDto>()

        return deleted.isNotEmpty()
    }

    suspend fun setElementChecked(elementId: String, checked: Boolean): Boolean {
        val updated = db.from(TABLE_SHOPPING_ELEMENTS)
            .update(UpdateElementChecked(checked)) {
                select()
                filter { eq(IdColumn, elementId) }
            }
            .decodeList<ShoppingElementDto>()

        return updated.isNotEmpty()
    }

    fun observeElementChanges(listId: String): Flow<ElementChange> {
        return flow {
            val channel = realtime.channel("elements-$listId")

            val changes =
                channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = TABLE_SHOPPING_ELEMENTS
                    filter(
                        "list_id",
                        FilterOperator.EQ,
                        listId
                    )
                }

            channel.subscribe(blockUntilSubscribed = true)
            emit(ElementChange.Connected)

            try {
                emitAll(
                    changes.mapNotNull { action ->
                        when (action) {
                            is PostgresAction.Insert ->
                                ElementChange.Upsert(
                                    action.decodeRecord<ShoppingElementDto>()
                                )

                            is PostgresAction.Update ->
                                ElementChange.Upsert(
                                    action.decodeRecord<ShoppingElementDto>()
                                )
                            // Deletes only carry the id of the removed row
                            is PostgresAction.Delete ->
                                action.oldRecord["id"]
                                    ?.jsonPrimitive
                                    ?.contentOrNull
                                    ?.let { ElementChange.Deleted(it) }

                            else -> null
                        }
                    }
                )
            } finally {
                // Cleanup is a suspend call, and the coroutine is already cancelled here
                withContext(NonCancellable) { realtime.removeChannel(channel) }
            }
        }
    }

    fun observeListChanges(): Flow<ListChange> {
        return flow {
            val channel = realtime.channel("lists-${UUID.randomUUID()}")

            val changes =
                channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = TABLE_SHOPPING_LISTS
                }

            channel.subscribe(blockUntilSubscribed = true)
            emit(ListChange.Connected)

            try {
                emitAll(
                    changes.mapNotNull { action ->
                        when (action) {
                            is PostgresAction.Insert ->
                                ListChange.Upsert(
                                    action.decodeRecord<ShoppingListDto>()
                                )

                            is PostgresAction.Update ->
                                ListChange.Upsert(
                                    action.decodeRecord<ShoppingListDto>()
                                )

                            is PostgresAction.Delete ->
                                action.oldRecord["id"]
                                    ?.jsonPrimitive
                                    ?.contentOrNull
                                    ?.let { ListChange.Deleted(it) }

                            else -> null
                        }
                    }
                )
            } finally {
                withContext(NonCancellable) { realtime.removeChannel(channel) }
            }
        }
    }
}