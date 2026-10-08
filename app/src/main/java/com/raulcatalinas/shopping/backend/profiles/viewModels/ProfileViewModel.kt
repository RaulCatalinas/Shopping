package com.raulcatalinas.shopping.backend.profiles.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulcatalinas.shopping.backend.profiles.repositories.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    var username by mutableStateOf("")
        private set

    var initialUsername by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun onUsernameChange(newName: String) {
        username = newName
    }

    fun isSameAsInitialUsername(): Boolean {
        return username.trim().equals(initialUsername.trim(), ignoreCase = true)
    }

    fun fetchUserProfile() {
        if (username.isNotBlank() || isLoading) return

        viewModelScope.launch {
            isLoading = true
            val profile = repository.getProfile()

            profile?.username?.let { fetchedName ->
                username = fetchedName
                initialUsername = fetchedName
            }
            isLoading = false
        }
    }

    fun updateUsername(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (username.isBlank()) {
                onResult(false)

                return@launch
            }

            isLoading = true
            val success = repository.updateUsername(username.trim())

            if (success) {
                initialUsername = username.trim()
            } else {
                val originalProfile = repository.getProfile()
                originalProfile?.username?.let {
                    username = it
                    initialUsername = it
                }
            }

            isLoading = false
            onResult(success)
        }
    }
}