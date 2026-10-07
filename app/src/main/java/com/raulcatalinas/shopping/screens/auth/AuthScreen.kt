package com.raulcatalinas.shopping.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulcatalinas.shopping.R
import com.raulcatalinas.shopping.backend.auth.utils.isAuthFormValid
import com.raulcatalinas.shopping.backend.auth.viewmodels.AuthViewModel
import com.raulcatalinas.shopping.screens.auth.enums.AuthMode
import com.raulcatalinas.shopping.shared.components.SegmentedButton
import com.raulcatalinas.shopping.shared.utils.showToast

@Composable
fun AuthScreen(viewModel: AuthViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var authMode by rememberSaveable { mutableStateOf(AuthMode.LOGIN) }

    var passwordHidden by rememberSaveable { mutableStateOf(true) }
    val userNameState = rememberTextFieldState()
    val emailState = rememberTextFieldState()
    val passwordState = rememberTextFieldState()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SegmentedButton(
                options = AuthMode.entries.toList(),
                selectedOption = authMode,
                onOptionSelected = { authMode = it },
                label = { it.label }
            )

            if (authMode == AuthMode.SIGN_UP) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    state = userNameState,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text
                    ),
                    placeholder = { Text("User Name") }
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
                enabled =
                    !isLoading
                            && isAuthFormValid(
                        authMode = authMode,
                        email = emailState.text.toString(),
                        password = passwordState.text.toString(),
                        userName = userNameState.text.toString()
                    ),
                onClick = {
                    val email = emailState.text.toString()
                    val password = passwordState.text.toString()

                    if (authMode == AuthMode.SIGN_UP) {
                        val userName = userNameState.text.toString()

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