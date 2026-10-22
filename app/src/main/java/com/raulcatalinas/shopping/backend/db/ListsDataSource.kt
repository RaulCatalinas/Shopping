package com.raulcatalinas.shopping.backend.db

import com.raulcatalinas.shopping.backend.db.constants.IdColumn
import com.raulcatalinas.shopping.backend.db.constants.TABLE_SHOPPING_LISTS
import com.raulcatalinas.shopping.backend.db.dto.ShoppingListDto
import com.raulcatalinas.shopping.core.lists.types.ListChange
import com.raulcatalinas.shopping.shared.types.NewShoppingList
import com.raulcatalinas.shopping.shared.types.UpdateShoppingList
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
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

// Talks to Supabase about the lists table: queries and its realtime changes.
class ListsDataSource @Inject constructor(
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
