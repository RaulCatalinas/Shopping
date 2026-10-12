package com.raulcatalinas.shopping.shared.extensions

import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.NoCredentialException
import io.github.jan.supabase.exceptions.HttpRequestException
import java.io.IOException

private const val GENERIC_ERROR = "Couldn't sign in with Google. Please try again."

fun Throwable?.toGoogleSignInMessage(): String? {
    if (this == null) return GENERIC_ERROR

    val causes = generateSequence(this) { it.cause }.toList()

    return when {
        // The user closed the account picker: not an error, show nothing
        // Must come BEFORE the generic cancellation case
        causes.any {
            it is GetCredentialCancellationException
                    && it.message?.contains("reauth", ignoreCase = true) == true
        } ->
            "Your Google account needs to be verified again. Open your phone's account settings, sign in to Google again, and retry."

        causes.any { it is GetCredentialCancellationException } ->
            "Google sign in was cancelled."

        // No Google account on the device
        causes.any { it is NoCredentialException } ->
            "No Google account found on this device. Add one in your phone settings and try again."

        // Google Play Services missing or outdated
        causes.any { it is GetCredentialProviderConfigurationException } ->
            "Google services aren't available on this device. Please update Google Play Services."

        // No connection, timeouts, DNS failures...
        causes.any { it is HttpRequestException || it is IOException } ->
            "Network error. Please check your connection and try again."

        else -> GENERIC_ERROR
    }
}