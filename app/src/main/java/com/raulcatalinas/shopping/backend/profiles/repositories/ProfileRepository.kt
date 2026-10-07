package com.raulcatalinas.shopping.backend.profiles.repositories

import com.raulcatalinas.shopping.backend.auth.repositories.AuthRepository
import com.raulcatalinas.shopping.backend.db.DbRepository
import com.raulcatalinas.shopping.shared.dto.UserProfileDto
import com.raulcatalinas.shopping.shared.types.UserProfile
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val authRepository: AuthRepository,
    private val dbRepository: DbRepository
) {
    suspend fun getProfile(): UserProfileDto? {
        val userId = authRepository.getCurrentUser()?.id ?: return null

        return dbRepository.getUserProfile(userId)
    }

    suspend fun updateUsername(newUsername: String): Boolean {
        return runCatching {
            val userId = authRepository.getCurrentUser()?.id
                ?: throw IllegalStateException("User not authenticated")

            dbRepository.updateUserProfile(
                userId, UserProfile(userId, newUsername)
            )
        }.isSuccess
    }
}