package com.raulcatalinas.shopping.backend.auth.viewmodels

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcatalinas.shopping.backend.auth.repositories.AuthRepository
import com.raulcatalinas.shopping.backend.auth.types.UsernameState
import com.raulcatalinas.shopping.backend.auth.viewmodels.constants.AUTH_VIEW_MODEL_TAG
import com.raulcatalinas.shopping.shared.extensions.containsWhiteSpace
import com.raulcatalinas.shopping.shared.extensions.isValidEmail
import com.raulcatalinas.shopping.shared.extensions.toGoogleSignInMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    private val _isLoading = MutableStateFlow(false)
    private val _usernameState = MutableStateFlow<UsernameState>(UsernameState.Idle)
    private val _usernameQuery = MutableStateFlow("")
    private val _isRecovering = MutableStateFlow(false)

    val isLoading = _isLoading.asStateFlow()
    val authState = _authState.asStateFlow()
    val usernameState = _usernameState.asStateFlow()

    init {
        observeAuthState()

        viewModelScope.launch {
            _usernameQuery
                .debounce(400.milliseconds)
                .distinctUntilChanged()
                .collect { query ->
                    val trimmed = query.trim()

                    if (trimmed.isEmpty()) {
                        _usernameState.value = UsernameState.Idle
                        return@collect
                    }

                    if (trimmed.length < 3 || trimmed.containsWhiteSpace()) {
                        _usernameState.value = UsernameState.Error("Invalid username format")

                        return@collect
                    }

                    checkUserNameExists(trimmed)
                }
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            combine(authRepository.sessionStatus, _isRecovering) { status, recovering ->
                if (recovering) {
                    return@combine if (status is SessionStatus.Authenticated) {
                        AuthState.PasswordRecovery
                    } else {
                        AuthState.Loading
                    }
                }

                when (status) {
                    SessionStatus.Initializing -> AuthState.Loading
                    is SessionStatus.Authenticated -> AuthState.Authenticated
                    is SessionStatus.NotAuthenticated -> AuthState.Unauthenticated
                    is SessionStatus.RefreshFailure -> AuthState.Unauthenticated
                }
            }
                .collect { _authState.value = it }
        }
    }

    fun signIn(
        email: String,
        password: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = try {
                if (!email.isValidEmail()) {
                    Log.e(
                        AUTH_VIEW_MODEL_TAG,
                        "Invalid email format: $email"
                    )

                    false
                } else {
                    authRepository.signIn(email, password)

                    true
                }
            } catch (e: Exception) {
                Log.e(
                    AUTH_VIEW_MODEL_TAG,
                    "Error signing in: ${e.message}",
                    e
                )

                false
            }

            _isLoading.value = false

            onResult(success)
        }
    }

    fun signUp(
        username: String,
        email: String,
        password: String,
        confirmedPassword: String,
        onResult: (success: Boolean, errorMessage: String?) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                when {
                    !email.isValidEmail() -> {
                        Log.e(
                            AUTH_VIEW_MODEL_TAG,
                            "Invalid email format: $email"
                        )

                        onResult(
                            false,
                            "Invalid email address format."
                        )

                        return@launch
                    }

                    password != confirmedPassword -> {
                        Log.e(
                            AUTH_VIEW_MODEL_TAG,
                            "Passwords do not match for user: $username"
                        )

                        onResult(
                            false,
                            "Passwords don't match."
                        )

                        return@launch
                    }
                }

                val result = authRepository.signUp(
                    username = username,
                    email = email,
                    password = password
                )

                if (result.isSuccess) {
                    onResult(
                        true,
                        null
                    )

                    return@launch
                }

                val error =
                    result.exceptionOrNull()?.message
                        ?: "An unexpected error occurred during sign up."

                Log.e(
                    AUTH_VIEW_MODEL_TAG,
                    "Error signing up: $error"
                )

                onResult(
                    false,
                    error
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInWithGoogle(
        context: Context,
        onResult: (success: Boolean, errorMessage: String?) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val result = authRepository.signInWithGoogle(context)

                if (result.isSuccess) {
                    onResult(true, null)

                    return@launch
                }

                val error = result.exceptionOrNull()

                Log.e(
                    AUTH_VIEW_MODEL_TAG,
                    "Error signing in with Google: ${error?.message}",
                    error
                )

                onResult(
                    false,
                    error.toGoogleSignInMessage()
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut(context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = try {
                authRepository.signOut(context)

                true
            } catch (e: Exception) {
                Log.e(
                    AUTH_VIEW_MODEL_TAG,
                    "Error signing out: ${e.message}",
                    e
                )

                false
            }

            _isLoading.value = false

            onResult(success)
        }
    }

    fun deleteAccount(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            val success = try {
                authRepository.deleteAccount()

                true
            } catch (e: Exception) {
                Log.e(
                    AUTH_VIEW_MODEL_TAG,
                    "Error deleting account: ${e.message}",
                    e
                )

                false
            }

            _isLoading.value = false

            onResult(success)
        }
    }

    fun checkUserNameExists(userName: String) {
        val cleanUsername = userName.trim()

        viewModelScope.launch {
            _usernameState.value = UsernameState.Checking

            val result = authRepository.checkUserNameExists(cleanUsername)

            if (result.isFailure) {
                val exception = result.exceptionOrNull()

                Log.e(
                    AUTH_VIEW_MODEL_TAG,
                    "Error checking username: ${exception?.message}",
                    exception
                )

                _usernameState.value = UsernameState.Error(exception?.message ?: "Unknown error")

                return@launch
            }

            val exists = result.getOrDefault(false)

            _usernameState.value = if (exists) {
                UsernameState.Taken(cleanUsername)
            } else {
                UsernameState.Available(cleanUsername)
            }
        }
    }

    fun sentResetPasswordEmail(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            val success = try {
                if (!email.isValidEmail()) {
                    Log.e(
                        AUTH_VIEW_MODEL_TAG,
                        "Invalid email format: $email"
                    )

                    false
                } else {
                    authRepository.sentResetPasswordEmail(email)

                    true
                }
            } catch (e: Exception) {
                Log.e(
                    AUTH_VIEW_MODEL_TAG,
                    "Error resetting password: ${e.message}",
                    e
                )
                false
            }

            _isLoading.value = false

            onResult(success)
        }
    }

    fun onUsernameTyped(query: String) {
        _usernameQuery.value = query
    }

    fun resetPassword(
        newPassword: String,
        confirmedNewPassword: String,
        onResult: (success: Boolean, errorMessage: String?) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                if (newPassword != confirmedNewPassword) {
                    Log.e(
                        AUTH_VIEW_MODEL_TAG,
                        "Passwords don't match for reset password flow"
                    )

                    onResult(
                        false,
                        "Passwords don't match."
                    )

                    return@launch
                }

                val result = authRepository.resetPassword(newPassword)

                if (result.isSuccess) {
                    onResult(
                        true,
                        null
                    )

                    return@launch
                }

                val rawError = result.exceptionOrNull()?.message

                Log.e(
                    AUTH_VIEW_MODEL_TAG,
                    "Error resetting password: $rawError"
                )

                val userFriendlyMessage = when {
                    rawError?.contains("same password", ignoreCase = true) == true -> {
                        "New password cannot be the same as your old password."
                    }

                    rawError?.contains("network", ignoreCase = true) == true -> {
                        "Network error. Please check your connection and try again."
                    }

                    else -> {
                        "Failed to reset password. Please try requesting a new reset link."
                    }
                }

                onResult(
                    false,
                    userFriendlyMessage
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun handleDeepLink(
        intent: Intent,
        onResult: (success: Boolean, errorMessage: String?) -> Unit
    ) {
        _isRecovering.value = true

        viewModelScope.launch {
            _isLoading.value = true

            try {
                val result = authRepository.handleDeepLink(intent)

                if (result.isSuccess) {
                    onResult(
                        true,
                        null
                    )

                    return@launch
                }

                _isRecovering.value = false

                val rawError = result.exceptionOrNull()?.message

                Log.e(
                    AUTH_VIEW_MODEL_TAG,
                    "Error handling deep link: $rawError"
                )

                val userFriendlyMessage = when {
                    rawError.isNullOrBlank() -> {
                        "Unable to verify the password reset link. Please request a new one."
                    }

                    rawError.contains("network", ignoreCase = true)
                            || rawError.contains("timeout", ignoreCase = true)
                            || rawError.contains("connection", ignoreCase = true)
                        -> {
                        "Network error. Please check your connection and try again."
                    }

                    rawError.contains("expired", ignoreCase = true)
                            || rawError.contains("invalid token", ignoreCase = true)
                            || rawError.contains("otp", ignoreCase = true)
                            || rawError.contains("code verifier", ignoreCase = true)
                        -> {
                        "This password reset link is invalid or has expired. Please request a new one."
                    }

                    else -> {
                        "Unable to verify the password reset link. Please request a new one and try again."
                    }
                }

                onResult(
                    false,
                    userFriendlyMessage
                )
            } catch (e: Exception) {
                _isRecovering.value = false

                throw e
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun finishRecovery(signOut: Boolean, context: Context) {
        viewModelScope.launch {
            if (signOut) {
                try {
                    authRepository.signOut(context)
                } catch (e: Exception) {
                    Log.e(
                        AUTH_VIEW_MODEL_TAG,
                        "Error signing out after recovery: ${e.message}",
                        e
                    )
                }
            }

            _isRecovering.value = false
        }
    }
}

