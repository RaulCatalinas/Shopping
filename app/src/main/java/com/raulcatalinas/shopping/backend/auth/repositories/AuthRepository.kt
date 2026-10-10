package com.raulcatalinas.shopping.backend.auth.repositories

import android.util.Log
import com.raulcatalinas.shopping.backend.auth.repositories.constants.AUTH_REPOSITORY_TAG
import com.raulcatalinas.shopping.backend.auth.repositories.constants.RESET_PASSWORD_REDIRECT_URL
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
        username: String,
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            val user = auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val userId = user?.id
                ?: auth.currentUserOrNull()?.id
                ?: return Result.failure(
                    IllegalStateException("User ID wasn't returned")
                )

            val profile = UserProfile(
                id = userId,
                username = username.trim()
            )

            val createdProfile = dbRepository.createUserProfile(profile)

            if (createdProfile == null) {
                Result.failure(
                    IllegalStateException("Couldn't create user profile")
                )
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
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

    suspend fun sentResetPasswordEmail(email: String) {
        auth.resetPasswordForEmail(email, RESET_PASSWORD_REDIRECT_URL)
    }

    suspend fun resetPassword(newPassword: String): Result<Unit> {
        return try {
            auth.updateUser {
                password = newPassword
            }

            Log.d(
                AUTH_REPOSITORY_TAG,
                "Password updated successfully"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(
                AUTH_REPOSITORY_TAG,
                "Error updating password: ${e.message}",
                e
            )

            Result.failure(e)
        }
    }
}