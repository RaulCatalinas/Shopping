package com.raulcatalinas.shopping.backend.db

import com.raulcatalinas.shopping.backend.db.constants.IdColumn
import com.raulcatalinas.shopping.backend.db.constants.ListIdColumn
import com.raulcatalinas.shopping.backend.db.constants.TABLE_SHOPPING_ELEMENTS
import com.raulcatalinas.shopping.backend.db.dto.ShoppingElementDto
import com.raulcatalinas.shopping.core.lists.types.ElementChange
import com.raulcatalinas.shopping.shared.types.NewShoppingElement
import com.raulcatalinas.shopping.shared.types.UpdateElementChecked
import com.raulcatalinas.shopping.shared.types.UpdateShoppingElement
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
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
import javax.inject.Inject

// Talks to Supabase about the shopping elements table: queries and its realtime changes.
class ElementsDataSource @Inject constructor(
    private val db: Postgrest,
    private val realtime: Realtime
) {
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
}
