package com.raulcatalinas.shopping.backend.auth.viewmodels.extensions

import androidx.annotation.StringRes
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.NoCredentialException
import com.raulcatalinas.shopping.R
import io.github.jan.supabase.exceptions.HttpRequestException
import java.io.IOException

@StringRes
fun Throwable?.toGoogleSignInMessage(): Int {
    if (this == null) return R.string.google_error_generic

    val causes = generateSequence(this) { it.cause }.toList()

    return when {
        // Cancellation that is really "the account needs to sign in again".
        // Must come BEFORE the generic cancellation case
        causes.any {
            it is GetCredentialCancellationException
                    && it.message?.contains("reauth", ignoreCase = true) == true
        } -> R.string.google_error_reauth

        causes.any { it is GetCredentialCancellationException } ->
            R.string.google_error_cancelled

        causes.any { it is NoCredentialException } ->
            R.string.google_error_no_account

        causes.any { it is GetCredentialProviderConfigurationException } ->
            R.string.google_error_unavailable

        causes.any { it is HttpRequestException || it is IOException } ->
            R.string.google_error_network

        else -> R.string.google_error_generic
    }
}