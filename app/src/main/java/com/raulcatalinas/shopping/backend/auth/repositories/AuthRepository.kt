package com.raulcatalinas.shopping.backend.auth.repositories

import android.content.Context
import android.content.Intent
import android.util.Log
import com.raulcatalinas.shopping.backend.auth.providers.GoogleProvider
import com.raulcatalinas.shopping.backend.auth.repositories.constants.AUTH_REPOSITORY_TAG
import com.raulcatalinas.shopping.backend.auth.repositories.constants.GOOGLE_AUTH_REDIRECT_URL
import com.raulcatalinas.shopping.backend.auth.repositories.constants.RESET_PASSWORD_REDIRECT_URL
import com.raulcatalinas.shopping.backend.auth.repositories.constants.WHITE_SPACES_REGEX
import com.raulcatalinas.shopping.backend.db.DbRepository
import com.raulcatalinas.shopping.shared.types.UserProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: Auth,
    private val dbRepository: DbRepository,
    private val supabaseClient: SupabaseClient,
    private val googleProvider: GoogleProvider
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

    suspend fun signInWithGoogle(context: Context): Result<Unit> {
        return try {
            val googleUserResult = googleProvider.getGoogleUser(context)

            if (googleUserResult.isFailure) {
                return Result.failure(
                    googleUserResult.exceptionOrNull() ?: Exception("Google sign in failed")
                )
            }

            val googleUser = googleUserResult.getOrThrow()

            auth.signInWith(
                provider = IDToken,
                redirectUrl = GOOGLE_AUTH_REDIRECT_URL
            ) {
                idToken = googleUser.idToken
                provider = Google
            }

            val userId =
                getCurrentUser()
                    ?.id
                    ?: return Result.failure(
                        IllegalStateException("User ID wasn't returned after Google sign in")
                    )

            val usernameToSave =
                googleUser
                    .name
                    ?.takeIf { it.isNotBlank() }
                    ?.replace(WHITE_SPACES_REGEX, "")
                    ?: "User_${userId.take(6)}"

            dbRepository
                .ensureUserProfile(usernameToSave)
                .getOrElse { return Result.failure(it) }

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(AUTH_REPOSITORY_TAG, "Error signing in with google: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signOut(context: Context) {
        googleProvider.signOut(context)
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

    fun handleDeepLink(intent: Intent): Result<Unit> {
        return try {
            supabaseClient.handleDeeplinks(intent)

            Log.d(AUTH_REPOSITORY_TAG, "Deep link handle successfully")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(
                AUTH_REPOSITORY_TAG,
                "Error handling deep link: ${e.message}",
                e
            )

            Result.failure(e)
        }
    }
}