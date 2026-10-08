package com.raulcatalinas.shopping.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import com.raulcatalinas.shopping.shared.components.CharacterCounter
import com.raulcatalinas.shopping.shared.components.RequiredFieldsNote
import com.raulcatalinas.shopping.shared.components.SegmentedButton
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
fun AuthScreen(viewModel: AuthViewModel = hiltViewModel()) {
    val context = LocalContext.current

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val usernameState by viewModel.usernameState.collectAsStateWithLifecycle()

    var authMode by rememberSaveable { mutableStateOf(AuthMode.LOGIN) }
    var passwordHidden by rememberSaveable { mutableStateOf(true) }
    var userNameMinimumCharsReached by rememberSaveable { mutableStateOf(false) }
    var passwordMinimumCharsReached by rememberSaveable { mutableStateOf(false) }
    var usernameLength by rememberSaveable { mutableIntStateOf(0) }
    var passwordLength by rememberSaveable { mutableIntStateOf(0) }

    val usernameTextFieldState = rememberTextFieldState()
    val emailState = rememberTextFieldState()
    val passwordState = rememberTextFieldState()
    val scrollState = rememberScrollState()

    val containsWhiteSpace =
        usernameTextFieldState
            .text
            .toString()
            .containsWhiteSpace()
    val isUsernameValid = usernameState is UsernameState.Available && !containsWhiteSpace

    val isFormValid = if (authMode == AuthMode.SIGN_UP) {
        userNameMinimumCharsReached && passwordMinimumCharsReached && isUsernameValid
    } else {
        passwordMinimumCharsReached
    }

    LaunchedEffect(usernameTextFieldState) {
        snapshotFlow { usernameTextFieldState.text.toString().trim() }
            .distinctUntilChanged()
            .debounce(400.milliseconds)
            .collect { query ->
                if (query.length < 3 || query.containsWhiteSpace()) {
                    return@collect
                }

                viewModel.checkUserNameExists(query)
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
            SegmentedButton(
                options = AuthMode.entries.toList(),
                selectedOption = authMode,
                onOptionSelected = { authMode = it },
                label = { it.label }
            )

            RequiredFieldsNote()

            if (authMode == AuthMode.SIGN_UP) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    state = usernameTextFieldState,
                    enabled = !isLoading,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text
                    ),
                    isError =
                        usernameState is UsernameState.Taken
                                || usernameState is UsernameState.Error
                                || containsWhiteSpace,
                    placeholder = { Text("Username") },
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
                    trailingIcon = trailingIcon@{
                        if (usernameTextFieldState.text.isBlank()) return@trailingIcon
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
                )
            }

            TextField(
                modifier = Modifier.fillMaxWidth(),
                state = emailState,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                placeholder = { Text("Email") },
            )

            SecureTextField(
                modifier = Modifier.fillMaxWidth(),
                state = passwordState,
                enabled = !isLoading,
                textObfuscationCharacter = '*',
                textObfuscationMode = if (passwordHidden) {
                    TextObfuscationMode.RevealLastTyped
                } else {
                    TextObfuscationMode.Visible
                },
                supportingText = {
                    CharacterCounter(
                        state = passwordState,
                        minimumCharacterCount = 8,
                        counterText = "$passwordLength / 8 min",
                        onTooShort = { passwordMinimumCharsReached = false },
                        onMinimumReached = { passwordMinimumCharsReached = true }
                    ) {
                        passwordLength = it
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { passwordHidden = !passwordHidden }) {
                        val icon = if (passwordHidden)
                            Icons.Filled.VisibilityOff
                        else
                            Icons.Filled.Visibility

                        Icon(
                            imageVector = icon,
                            contentDescription = if (passwordHidden) "Show password" else "Hide password"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                placeholder = { Text("Password") }
            )

            if (authMode == AuthMode.LOGIN) {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    onClick = { println("Resetting password...") }
                ) {
                    Text("Reset Password")
                }
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && isFormValid,
                onClick = {
                    val email = emailState.text.toString()
                    val password = passwordState.text.toString()

                    if (authMode == AuthMode.SIGN_UP) {
                        val userName = usernameTextFieldState.text.toString()

                        viewModel.signUp(userName, email, password) {
                            if (it) println("Sign up successful")
                            else showToast(context, "Invalid email or password")
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
