package com.raulcatalinas.shopping.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulcatalinas.shopping.backend.auth.viewmodels.AuthViewModel
import com.raulcatalinas.shopping.shared.components.PasswordInput
import com.raulcatalinas.shopping.shared.extensions.verticalScrollbar
import com.raulcatalinas.shopping.shared.utils.showToast

@Composable
fun ResetPasswordScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onPasswordResetSuccess: () -> Unit = {}
) {
    val context = LocalContext.current

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmedNewPassword by rememberSaveable { mutableStateOf("") }
    var passwordMinimumCharsReached by rememberSaveable { mutableStateOf(false) }
    var confirmedPasswordMinimumCharsReached by rememberSaveable { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val isFormValid =
        passwordMinimumCharsReached
                && confirmedPasswordMinimumCharsReached

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
                .verticalScrollbar(scrollState)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PasswordInput(
                enabled = !isLoading,
                onTooShort = { passwordMinimumCharsReached = false },
                onMinimumReached = { passwordMinimumCharsReached = true },
                placeHolder = "New Password"
            ) {
                newPassword = it
            }

            PasswordInput(
                enabled = !isLoading,
                onTooShort = { confirmedPasswordMinimumCharsReached = false },
                onMinimumReached = { confirmedPasswordMinimumCharsReached = true },
                placeHolder = "Confirm New Password"
            ) {
                confirmedNewPassword = it
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && isFormValid,
                onClick = {
                    viewModel.resetPassword(
                        newPassword,
                        confirmedNewPassword
                    ) { success, errorMessage ->
                        if (success) {
                            showToast(
                                context,
                                "Password successfully changed"
                            )

                            onPasswordResetSuccess()
                        } else {
                            showToast(
                                context,
                                errorMessage ?: "Failed to reset password"
                            )
                        }
                    }
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )

                    return@Button
                }

                Text("Save new password")
            }
        }
    }
}