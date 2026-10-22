package com.raulcatalinas.shopping.backend.db

import android.util.Log
import com.raulcatalinas.shopping.backend.db.constants.CHECK_USERNAME_EXISTS_RPC
import com.raulcatalinas.shopping.backend.db.constants.DB_REPOSITORY_TAG
import com.raulcatalinas.shopping.backend.db.constants.DELETE_USER_RPC
import com.raulcatalinas.shopping.backend.db.constants.ENSURE_USER_PROFILE_RPC
import com.raulcatalinas.shopping.backend.db.constants.IdColumn
import com.raulcatalinas.shopping.backend.db.constants.TABLE_SHOPPING_PROFILES
import com.raulcatalinas.shopping.backend.db.dto.CheckUsernameParams
import com.raulcatalinas.shopping.backend.db.dto.EnsureProfileParams
import com.raulcatalinas.shopping.backend.profiles.dto.UserProfileDto
import com.raulcatalinas.shopping.shared.types.UserProfile
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

// Talks to Supabase about profiles: queries and RPC functions. No business logic here.
class ProfilesDataSource @Inject constructor(
    private val db: Postgrest
) {
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
}
