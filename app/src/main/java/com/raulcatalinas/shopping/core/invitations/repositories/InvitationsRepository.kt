package com.raulcatalinas.shopping.core.invitations.repositories

import android.util.Log
import com.raulcatalinas.shopping.backend.db.InvitationsDataSource
import com.raulcatalinas.shopping.core.invitations.InvitationLinks
import com.raulcatalinas.shopping.core.invitations.constants.INVITATIONS_REPOSITORY_TAG
import com.raulcatalinas.shopping.core.invitations.types.InvitationException
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class InvitationsRepository @Inject constructor(
    private val invitationsDataSource: InvitationsDataSource
) {
    // Creates an invitation and returns the link ready to be shared
    suspend fun createInvitationLink(listId: String, validHours: Int): Result<String> {
        return try {
            val token = invitationsDataSource.createListInvitation(listId, validHours)

            Result.success(InvitationLinks.build(token))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // The token is never logged: it is the secret inside the link
            Log.e(
                INVITATIONS_REPOSITORY_TAG,
                "Error creating invitation for list $listId: ${e.message}",
                e
            )
            Result.failure(e.toInvitationException())
        }
    }

    // The SQL functions raise errors with fixed messages. Matching the text works
    // across library versions.
    private fun Exception.toInvitationException(): Exception {
        val text = message.orEmpty()

        return when {
            text.contains("invalid_or_expired") -> InvitationException.InvalidOrExpired()
            text.contains("not_allowed") -> InvitationException.NotAllowed()
            else -> this
        }
    }
}