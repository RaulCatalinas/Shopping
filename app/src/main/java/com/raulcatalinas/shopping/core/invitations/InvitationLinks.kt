package com.raulcatalinas.shopping.core.invitations

// The ONLY place that knows what an invitation link looks like.
// When the app moves from shopping://join/<token> to a https link, change BASE here.
object InvitationLinks {
    private const val BASE = "shopping://join/"

    // The token is 32 hex characters (an uuid without dashes), see create_list_invitation
    private val TOKEN_REGEX = Regex("[0-9a-fA-F]{32}")

    fun build(token: String): String = "$BASE$token"

    // Accepts a full link or just the token, so it works both when the app opens a
    // link and when the user pastes something in "Join a list".
    // Returns null if the text does not contain a valid token.
    fun extractToken(input: String): String? {
        val candidate =
            input
                .trim()
                .substringBefore('#')
                .substringBefore('?')
                .trimEnd('/')
                .substringAfterLast('/')

        return candidate.takeIf { TOKEN_REGEX.matches(it) }
    }
}