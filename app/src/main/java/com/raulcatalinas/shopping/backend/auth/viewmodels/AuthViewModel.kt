package com.raulcatalinas.shopping.backend.auth.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcatalinas.shopping.backend.auth.repositories.AuthRepository
import com.raulcatalinas.shopping.backend.auth.viewmodels.constants.AUTH_VIEW_MODEL_TAG
import com.raulcatalinas.shopping.shared.utils.isValidEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    private val _isLoading = MutableStateFlow(false)

    val isLoading = _isLoading.asStateFlow()
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.sessionStatus.collect { status ->
                _authState.value = when (status) {
                    SessionStatus.Initializing -> AuthState.Loading
                    is SessionStatus.Authenticated -> AuthState.Authenticated
                    is SessionStatus.NotAuthenticated -> AuthState.Unauthenticated
                    is SessionStatus.RefreshFailure -> AuthState.Unauthenticated
                }
            }
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
                if (!isValidEmail(email)) {
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
        userName: String,
        email: String,
        password: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = try {
                if (!isValidEmail(email)) {
                    Log.e(
                        AUTH_VIEW_MODEL_TAG,
                        "Invalid email format: $email"
                    )

                    false
                } else {
                    authRepository.signUp(
                        userName = userName,
                        email = email,
                        password = password
                    )

                    true
                }
            } catch (e: Exception) {
                Log.e(
                    AUTH_VIEW_MODEL_TAG,
                    "Error signing up: ${e.message}",
                    e
                )

                false
            }

            _isLoading.value = false

            onResult(success)
        }
    }

    fun signInWithGoogle(
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = try {
                authRepository.signInWithGoogle()
                true
            } catch (e: Exception) {
                Log.e(
                    AUTH_VIEW_MODEL_TAG,
                    "Error signing in with Google: ${e.message}",
                    e
                )

                false
            }

            _isLoading.value = false

            onResult(success)
        }
    }

    fun signOut(
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = try {
                authRepository.signOut()

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
}