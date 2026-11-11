package com.raulcatalinas.shopping.core.invitations.types

sealed class InvitationException(message: String) : Exception(message) {
    class InvalidOrExpired : InvitationException("invalid_or_expired")
    class NotAllowed : InvitationException("not_allowed")
}