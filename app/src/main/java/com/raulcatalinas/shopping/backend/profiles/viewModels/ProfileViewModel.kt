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

    var isLoading by mutableStateOf(false)
        private set

    fun onUsernameChange(newName: String) {
        username = newName
    }

    fun fetchUserProfile() {
        if (username.isNotBlank() || isLoading) return

        viewModelScope.launch {
            isLoading = true
            val profile = repository.getProfile()

            profile?.userName?.let {
                username = it
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
            val success = repository.updateUsername(username)

            if (!success) {
                val originalProfile = repository.getProfile()
                originalProfile?.userName?.let { username = it }
            }

            isLoading = false
            onResult(success)
        }
    }
}