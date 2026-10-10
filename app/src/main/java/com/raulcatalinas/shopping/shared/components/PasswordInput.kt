package com.raulcatalinas.shopping.shared.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun PasswordInput(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeHolder: String = "Password",
    onTooShort: (() -> Unit)? = null,
    onMinimumReached: (() -> Unit)? = null,
    onPasswordTyping: (String) -> Unit
) {
    var passwordHidden by rememberSaveable { mutableStateOf(true) }
    var passwordLength by rememberSaveable { mutableIntStateOf(0) }

    val passwordState = rememberTextFieldState()

    LaunchedEffect(passwordState) {
        snapshotFlow { passwordState.text.toString().trim() }
            .collect { text ->
                onPasswordTyping(text)
            }
    }

    SecureTextField(
        modifier = modifier.fillMaxWidth(),
        state = passwordState,
        enabled = enabled,
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
                onTooShort = onTooShort,
                onMinimumReached = onMinimumReached
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
                    contentDescription =
                        if (passwordHidden) "Show password"
                        else "Hide password"
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password
        ),
        placeholder = { Text(placeHolder) },
    )
}