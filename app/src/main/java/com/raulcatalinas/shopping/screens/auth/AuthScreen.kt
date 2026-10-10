package com.raulcatalinas.shopping.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulcatalinas.shopping.R
import com.raulcatalinas.shopping.backend.auth.types.UsernameState
import com.raulcatalinas.shopping.backend.auth.viewmodels.AuthViewModel
import com.raulcatalinas.shopping.screens.auth.enums.AuthMode
import com.raulcatalinas.shopping.shared.components.Input
import com.raulcatalinas.shopping.shared.components.PasswordInput
import com.raulcatalinas.shopping.shared.components.RequiredFieldsNote
import com.raulcatalinas.shopping.shared.components.SegmentedButton
import com.raulcatalinas.shopping.shared.components.UserWarning
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
    } else {
        passwordMinimumCharsReached && email.isNotEmpty()
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
            SegmentedButton(
                options = AuthMode.entries.toList(),
                selectedOption = authMode,
                onOptionSelected = { authMode = it },
                label = { it.label }
            )

            RequiredFieldsNote()

            if (authMode == AuthMode.SIGN_UP) {
                Input(
                    enabled = !isLoading,
                    isError =
                        usernameState is UsernameState.Taken
                                || usernameState is UsernameState.Error
                                || containsWhiteSpace,
                    placeholder = "Username",
                    showCharacterCounter = true,
                    minimumCharacterCount = 3,
                    onTooShort = { userNameMinimumCharsReached = false },
                    onMinimumReached = { userNameMinimumCharsReached = true },
                    supportingText = { UserWarning("No spaces allowed") },
                    trailingIcon = trailingIcon@{
                        if (username.isBlank()) return@trailingIcon
                        if (containsWhiteSpace) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Invalid username format",
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
                ) {
                    username = it
                    viewModel.onUsernameTyped(it)
                }
            }

            Input(
                enabled = !isLoading,
                keyboardType = KeyboardType.Email,
                placeholder = "Email",
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
                    placeHolder = "Confirm Password"
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
                            showToast(context, "Email is required")

                            return@onClick
                        }

                        if (!email.isValidEmail()) {
                            showToast(context, "Please enter a valid email address")

                            return@onClick
                        }

                        viewModel.sentResetPasswordEmail(email) {
                            showToast(
                                context,
                                if (it) "Reset link sent to your email"
                                else "Failed to send reset email. Please try again."
                            )
                        }
                    }
                ) {
                    Text("Reset Password")
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
                        else showToast(context, "Invalid email or password")
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

                Text(if (authMode == AuthMode.SIGN_UP) "Sign Up" else "Login")
            }

            OutlinedButton(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(8.dp)
                    .fillMaxWidth(),
                enabled = !isLoading,
                onClick = {
                    viewModel.signInWithGoogle { success ->
                        println(
                            if (success) "Sign in with Google successful"
                            else showToast(context, "Sign in with Google failed")
                        )
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
                        contentDescription = "Google Icon"
                    )

                    Text("Login with Google")
                }
            }
        }
    }
}
