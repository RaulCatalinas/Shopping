package com.raulcatalinas.shopping.backend.auth.repositories

import com.raulcatalinas.shopping.backend.db.DbRepository
import com.raulcatalinas.shopping.shared.types.UserProfile
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: Auth,
    private val dbRepository: DbRepository
) {
    val sessionStatus: Flow<SessionStatus>
        get() = auth.sessionStatus

    suspend fun signIn(
        email: String,
        password: String
    ) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signUp(
        userName: String,
        email: String,
        password: String
    ) {
        val user = auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }

        val userId = user?.id
            ?: auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User ID was not returned")

        val profile = UserProfile(
            id = userId,
            username = userName.trim()
        )

        dbRepository.createUserProfile(profile)
            ?: throw IllegalStateException("Couldn't create user profile")
    }

    suspend fun signInWithGoogle() {
        println("Signing in with google...")
    }

    suspend fun signOut() {
        auth.signOut()
    }

    suspend fun deleteAccount() {
        val userId = getCurrentUser()?.id
            ?: throw IllegalStateException("User ID was not returned")

        dbRepository.deleteAccount(userId)
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUserOrNull()

    suspend fun checkUserNameExists(userName: String): Result<Boolean> {
        return dbRepository.checkUserNameExists(userName)
    }
}