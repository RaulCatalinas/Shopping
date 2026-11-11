package com.raulcatalinas.shopping.backend.db

import com.raulcatalinas.shopping.backend.db.constants.CREATE_LIST_INVITATION_RPC
import com.raulcatalinas.shopping.backend.db.dto.CreateInvitationParams
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.rpc
import javax.inject.Inject

class InvitationsDataSource @Inject constructor(
    private val db: Postgrest
) {
    suspend fun createListInvitation(listId: String, validHours: Int): String {
        return db
            .rpc(
                function = CREATE_LIST_INVITATION_RPC,
                parameters = CreateInvitationParams(
                    listId = listId,
                    validHours = validHours
                )
            )
            .decodeAs<String>()
    }
}