package com.raulcatalinas.shopping.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulcatalinas.shopping.backend.auth.types.UsernameState
import com.raulcatalinas.shopping.backend.auth.viewmodels.AuthViewModel
import com.raulcatalinas.shopping.backend.profiles.viewModels.ProfileViewModel
import com.raulcatalinas.shopping.shared.components.CharacterCounter
import com.raulcatalinas.shopping.shared.components.ConfirmDialog
import com.raulcatalinas.shopping.shared.components.SectionHeader
import com.raulcatalinas.shopping.shared.components.UserWarning
import com.raulcatalinas.shopping.shared.extensions.containsWhiteSpace
import com.raulcatalinas.shopping.shared.extensions.verticalScrollbar
import com.raulcatalinas.shopping.shared.utils.showToast
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val usernameState by authViewModel.usernameState.collectAsStateWithLifecycle()

    var userNameMinimumCharsReached by rememberSaveable { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val usernameTextFieldState = rememberTextFieldState()

    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var usernameLength by rememberSaveable { mutableIntStateOf(0) }

    val isSameUsername = profileViewModel.isSameAsInitialUsername()
    val containsWhiteSpace =
        usernameTextFieldState
            .text
            .toString()
            .containsWhiteSpace()
    val isUsernameValid =
        (isSameUsername || usernameState is UsernameState.Available)
                && !containsWhiteSpace

    val canSave =
        !profileViewModel.isLoading && userNameMinimumCharsReached && isUsernameValid && !isSameUsername

    LaunchedEffect(Unit) {
        profileViewModel.fetchUserProfile()
    }

    LaunchedEffect(usernameTextFieldState) {
        snapshotFlow { usernameTextFieldState.text.toString().trim() }
            .distinctUntilChanged()
            .debounce(400.milliseconds)
            .collect { newUsername ->
                profileViewModel.onUsernameChange(newUsername)

                if (
                    newUsername.length < 3
                    || newUsername.containsWhiteSpace()
                    || profileViewModel.isSameAsInitialUsername()
                ) {
                    return@collect
                }

                authViewModel.checkUserNameExists(newUsername)
            }
    }

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
            Text(
                text = if (profileViewModel.username.isNotBlank()) "Hello, ${profileViewModel.username}!" else "Your Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            SectionHeader(title = "Profile details")

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                state = usernameTextFieldState,
                enabled = !profileViewModel.isLoading,
                lineLimits = TextFieldLineLimits.SingleLine,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                ),
                placeholder = { Text("e.g. JohnDoe") },
                isError = usernameState is UsernameState.Taken || usernameState is UsernameState.Error,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserWarning("No spaces allowed")

                        CharacterCounter(
                            state = usernameTextFieldState,
                            minimumCharacterCount = 3,
                            counterText = "$usernameLength / 3 min",
                            onTooShort = { userNameMinimumCharsReached = false },
                            onMinimumReached = { userNameMinimumCharsReached = true }
                        ) {
                            usernameLength = it
                        }
                    }
                },
                trailingIcon = {
                    if (!isSameUsername) {
                        when (usernameState) {
                            UsernameState.Checking -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            }

                            is UsernameState.Available -> {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Username available",
                                    tint = Color(0xFF34C759)
                                )
                            }

                            is UsernameState.Taken, is UsernameState.Error -> {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Username unavailable",
                                    tint = Color(0xFFFF3B30)
                                )
                            }

                            else -> {}
                        }
                    }
                }
            )

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    profileViewModel.updateUsername { success ->
                        showToast(
                            context,
                            if (success) "Username updated successfully"
                            else "Failed to update username"
                        )
                    }
                },
                enabled = canSave
            ) {
                if (profileViewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    return@OutlinedButton
                }

                Text("Save changes")
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            SectionHeader(title = "Account")

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showSignOutDialog = true },
            ) {
                Text("Log out")
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showDeleteAccountDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Delete account")
            }
        }

        if (showSignOutDialog) {
            ConfirmDialog(
                title = "Log out",
                content = "Are you sure you want to log out?",
            ) {
                showSignOutDialog = false
                authViewModel.signOut { success ->
                    showToast(
                        context,
                        if (success) "Session closed successfully"
                        else "We were unable to log you out; please try again later"
                    )
                }
            }
        }

        if (showDeleteAccountDialog) {
            ConfirmDialog(
                title = "Delete account",
                content = "Are you sure you want to delete your account? This action cannot be undone.",
            ) {
                showDeleteAccountDialog = false
                authViewModel.deleteAccount { success ->
                    showToast(
                        context,
                        if (success) "Account deleted successfully"
                        else "We were unable to delete your account; please try again later"
                    )
                }
            }
        }
    }
}