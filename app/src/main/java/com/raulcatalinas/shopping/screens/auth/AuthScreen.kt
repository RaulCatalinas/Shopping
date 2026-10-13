package com.raulcatalinas.shopping.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulcatalinas.shopping.R
import com.raulcatalinas.shopping.backend.auth.types.UsernameState
import com.raulcatalinas.shopping.backend.auth.viewmodels.AuthViewModel
import com.raulcatalinas.shopping.shared.components.Input
import com.raulcatalinas.shopping.shared.components.PasswordInput
import com.raulcatalinas.shopping.shared.components.RequiredFieldsNote
import com.raulcatalinas.shopping.shared.components.SegmentedButton
import com.raulcatalinas.shopping.shared.components.UserWarning
import com.raulcatalinas.shopping.shared.enums.AuthMode
import com.raulcatalinas.shopping.shared.extensions.containsWhiteSpace
import com.raulcatalinas.shopping.shared.extensions.isValidEmail
import com.raulcatalinas.shopping.shared.extensions.verticalScrollbar
import com.raulcatalinas.shopping.shared.utils.showToast

@Composable
fun AuthScreen(viewModel: AuthViewModel = hiltViewModel()) {
    val context = LocalContext.current

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val usernameState by viewModel.usernameState.collectAsStateWithLifecycle()

    var authMode by rememberSaveable { mutableStateOf(AuthMode.LOGIN) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmedPassword by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var userNameMinimumCharsReached by rememberSaveable { mutableStateOf(false) }
    var passwordMinimumCharsReached by rememberSaveable { mutableStateOf(false) }
    var confirmedPasswordMinimumCharsReached by rememberSaveable { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val containsWhiteSpace = username.containsWhiteSpace()
    val isUsernameValid =
        usernameState is UsernameState.Available
                && !containsWhiteSpace

    val isFormValid = if (authMode == AuthMode.SIGN_UP) {
        userNameMinimumCharsReached
                && passwordMinimumCharsReached
                && confirmedPasswordMinimumCharsReached
                && isUsernameValid
                && email.isNotEmpty()
                && !password.containsWhiteSpace()
                && !confirmedPassword.containsWhiteSpace()
    } else {
        passwordMinimumCharsReached
                && email.isNotEmpty()
                && !password.containsWhiteSpace()
    }

    val emailIsRequiredMessage = stringResource(R.string.email_is_required)
    val enterValidEmailAddressMessage = stringResource(R.string.enter_valid_email_address)
    val resetLinkSentSuccessfullyMessage = stringResource(R.string.reset_link_sent_successfully)
    val resetLinkSentFailedMessage = stringResource(R.string.reset_link_sent_failed)
    val loginFailedMessage = stringResource(R.string.login_failed)

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
            SegmentedButton(
                options = AuthMode.entries.toList(),
                selectedOption = authMode,
                onOptionSelected = { authMode = it },
                label = { stringResource(it.labelRes) },
                enabled = !isLoading
            )

            RequiredFieldsNote()

            if (authMode == AuthMode.SIGN_UP) {
                Input(
                    enabled = !isLoading,
                    isError =
                        usernameState is UsernameState.Taken
                                || usernameState is UsernameState.Error
                                || containsWhiteSpace,
                    placeholder = stringResource(R.string.username),
                    showCharacterCounter = true,
                    minimumCharacterCount = 3,
                    onTooShort = { userNameMinimumCharsReached = false },
                    onMinimumReached = { userNameMinimumCharsReached = true },
                    supportingText = {
                        UserWarning(
                            stringResource(
                                R.string.no_spaces_allowed
                            )
                        )
                    },
                    trailingIcon = trailingIcon@{
                        if (username.isBlank()) return@trailingIcon
                        if (containsWhiteSpace) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = stringResource(
                                    R.string.invalid_username_format
                                ),
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
                                    contentDescription = stringResource(
                                        R.string.username_available
                                    ),
                                    tint = Color(0xFF34C759)
                                )
                            }

                            is UsernameState.Taken, is UsernameState.Error -> {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = stringResource(
                                        R.string.username_unavailable
                                    ),
                                    tint = Color(0xFFFF3B30)
                                )
                            }

                            else -> {}
                        }
                    }
                ) {
                    username = it
                    viewModel.onUsernameTyped(it)
                }
            }

            Input(
                enabled = !isLoading,
                keyboardType = KeyboardType.Email,
                placeholder = stringResource(R.string.email),
            ) {
                email = it
            }

            PasswordInput(
                enabled = !isLoading,
                onTooShort = { passwordMinimumCharsReached = false },
                onMinimumReached = { passwordMinimumCharsReached = true }
            ) {
                password = it
            }

            if (authMode == AuthMode.SIGN_UP) {
                PasswordInput(
                    enabled = !isLoading,
                    onTooShort = { confirmedPasswordMinimumCharsReached = false },
                    onMinimumReached = { confirmedPasswordMinimumCharsReached = true },
                    placeHolder = stringResource(R.string.confirm_password)
                ) {
                    confirmedPassword = it
                }
            }

            if (authMode == AuthMode.LOGIN) {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    onClick = onClick@{
                        if (email.containsWhiteSpace()) {
                            showToast(context, emailIsRequiredMessage)

                            return@onClick
                        }

                        if (!email.isValidEmail()) {
                            showToast(context, enterValidEmailAddressMessage)

                            return@onClick
                        }

                        viewModel.sentResetPasswordEmail(email) {
                            showToast(
                                context,
                                if (it) resetLinkSentSuccessfullyMessage
                                else resetLinkSentFailedMessage
                            )
                        }
                    }
                ) {
                    Text(stringResource(R.string.reset_password))
                }
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && isFormValid,
                onClick = {
                    if (authMode == AuthMode.SIGN_UP) {
                        viewModel.signUp(
                            username = username,
                            email = email,
                            password = password,
                            confirmedPassword = confirmedPassword
                        ) { success, errorMessage ->
                            if (success) println("Sign up successful")
                            else showToast(context, errorMessage!!)
                        }

                        return@Button
                    }

                    viewModel.signIn(email, password) {
                        if (it) println("Login successful")
                        else showToast(context, loginFailedMessage)
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

                Text(
                    if (authMode == AuthMode.SIGN_UP) stringResource(
                        R.string.sign_up
                    )
                    else stringResource(R.string.login)
                )
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                onClick = {
                    viewModel.signInWithGoogle(context) { success, errorMessage ->
                        if (success) println("Login with Google successful")
                        else errorMessage?.let { showToast(context, it) }
                    }
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )

                    return@OutlinedButton
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.google_icon),
                        contentDescription = stringResource(R.string.google_icon)
                    )

                    Text(stringResource(R.string.login_with_google))
                }
            }
        }
    }
}
