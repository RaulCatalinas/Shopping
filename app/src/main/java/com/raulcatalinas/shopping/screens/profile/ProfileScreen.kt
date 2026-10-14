package com.raulcatalinas.shopping.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldLineLimits
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulcatalinas.shopping.R
import com.raulcatalinas.shopping.backend.auth.types.UsernameState
import com.raulcatalinas.shopping.backend.auth.viewmodels.AuthViewModel
import com.raulcatalinas.shopping.backend.profiles.viewModels.ProfileViewModel
import com.raulcatalinas.shopping.shared.components.ConfirmDialog
import com.raulcatalinas.shopping.shared.components.Input
import com.raulcatalinas.shopping.shared.components.RequiredFieldsNote
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

    var username by remember { mutableStateOf("") }
    var clearSignal by remember { mutableIntStateOf(0) }

    val isSameUsername = profileViewModel.isSameAsInitialUsername()
    val containsWhiteSpace = username.containsWhiteSpace()
    val isUsernameValid =
        (isSameUsername || usernameState is UsernameState.Available)
                && !containsWhiteSpace

    val canSave =
        !profileViewModel.isLoading && userNameMinimumCharsReached && isUsernameValid && !isSameUsername

    val usernameUpdatedMessage = stringResource(R.string.username_updated)
    val usernameUpdateFailedMessage = stringResource(R.string.username_update_failed)
    val seasonCloseSuccessfully = stringResource(R.string.session_closed_successfully)
    val seasonCloseFailed = stringResource(R.string.session_closed_failed)
    val accountDeletedSuccessfully = stringResource(R.string.account_deleted_successfully)
    val accountDeletedFailed = stringResource(R.string.account_deleted_failed)

    LaunchedEffect(Unit) {
        profileViewModel.fetchUserProfile()
    }

    LaunchedEffect(Unit) {
        snapshotFlow { username }
            .distinctUntilChanged()
            .debounce(400.milliseconds)
            .collect { newUsername ->
                if (
                    newUsername.length < 3
                    || newUsername.containsWhiteSpace()
                    || profileViewModel.isSameAsInitialUsername()
                ) return@collect

                authViewModel.checkUserNameExists(newUsername)
            }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScrollbar(scrollState)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text =
                    if (profileViewModel.username.isNotBlank()) stringResource(
                        R.string.hello_username,
                        profileViewModel.username
                    )
                    else stringResource(R.string.your_profile),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            SectionHeader(title = stringResource(R.string.profile_details))

            RequiredFieldsNote()

            Input(
                isOutlined = true,
                enabled = !profileViewModel.isLoading,
                lineLimits = TextFieldLineLimits.SingleLine,
                placeholder = stringResource(R.string.example_username),
                showCharacterCounter = true,
                minimumCharacterCount = 3,
                clearSignal = clearSignal,
                onTooShort = { userNameMinimumCharsReached = false },
                onMinimumReached = { userNameMinimumCharsReached = true },
                isError =
                    usernameState is UsernameState.Taken
                            || usernameState is UsernameState.Error
                            || containsWhiteSpace,
                supportingText = {
                    UserWarning(
                        stringResource(R.string.no_spaces_allowed)
                    )
                },
                trailingIcon = trailingIcon@{
                    if (isSameUsername) return@trailingIcon
                    if (username.isBlank()) return@trailingIcon
                    if (containsWhiteSpace) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription =
                                stringResource(R.string.invalid_username_format),
                            tint = Color(0xFFFF3B30)
                        )

                        return@trailingIcon
                    }

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
                                contentDescription =
                                    stringResource(R.string.username_available),
                                tint = Color(0xFF34C759)
                            )
                        }

                        is UsernameState.Taken, is UsernameState.Error -> {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription =
                                    stringResource(R.string.username_unavailable),
                                tint = Color(0xFFFF3B30)
                            )
                        }

                        else -> {}
                    }
                }
            ) {
                username = it
                profileViewModel.onUsernameChange(it)
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    profileViewModel.updateUsername { success ->
                        if (success) {
                            clearSignal++
                            showToast(context, usernameUpdatedMessage)

                            return@updateUsername
                        }

                        showToast(context, usernameUpdateFailedMessage)
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

                Text(stringResource(R.string.save_changes))
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            SectionHeader(title = stringResource(R.string.account))

            ConfirmDialog(
                title = stringResource(R.string.log_out),
                content = stringResource(R.string.log_out_confirm_dialog_body),
                onConfirm = {
                    authViewModel.signOut(context) { success ->
                        showToast(
                            context,
                            if (success) seasonCloseSuccessfully
                            else seasonCloseFailed
                        )
                    }
                },
                trigger = { open ->
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = open
                    ) {
                        Text(stringResource(R.string.log_out))
                    }
                }
            )

            ConfirmDialog(
                title = stringResource(R.string.delete_account),
                content = stringResource(
                    R.string.delete_account_confirm_dialog_body
                ),
                isDestructiveAction = true,
                confirmText = stringResource(R.string.delete_btn),
                dismissText = stringResource(R.string.keep_account_btn),
                onConfirm = {
                    authViewModel.deleteAccount { success ->
                        showToast(
                            context,
                            if (success) accountDeletedSuccessfully
                            else accountDeletedFailed
                        )
                    }
                },
                trigger = { open ->
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = open,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text(stringResource(R.string.delete_account))
                    }
                }
            )
        }
    }
}