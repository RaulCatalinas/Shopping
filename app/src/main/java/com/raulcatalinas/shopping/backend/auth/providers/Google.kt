package com.raulcatalinas.shopping.backend.auth.providers

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.raulcatalinas.shopping.BuildConfig
import com.raulcatalinas.shopping.backend.auth.providers.constants.GOOGLE_PROVIDER_TAG
import com.raulcatalinas.shopping.backend.auth.providers.dto.GoogleUserDto
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class GoogleProvider @Inject constructor() {
    suspend fun getGoogleUser(context: Context): Result<GoogleUserDto> {
        val credentialManager = CredentialManager.create(context)

        val googleOption =
            GetSignInWithGoogleOption
                .Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .build()

        val request =
            GetCredentialRequest
                .Builder()
                .addCredentialOption(googleOption)
                .build()

        return try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (
                credential !is CustomCredential
                || credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                return Result.failure(
                    IllegalStateException(
                        "Unexpected credential type"
                    )
                )
            }

            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)

            Result.success(
                GoogleUserDto(
                    idToken = googleCredential.idToken,
                    name = googleCredential.displayName
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut(context: Context) {
        try {
            CredentialManager
                .create(context)
                .clearCredentialState(ClearCredentialStateRequest())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(GOOGLE_PROVIDER_TAG, "Error clearing credential state: ${e.message}", e)
        }
    }
}