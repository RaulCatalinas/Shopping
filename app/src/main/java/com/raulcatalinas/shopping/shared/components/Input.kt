package com.raulcatalinas.shopping.shared.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun Input(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    showCharacterCounter: Boolean = false,
    minimumCharacterCount: Int? = null,
    placeholder: String,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    onTooShort: (() -> Unit)? = null,
    onMinimumReached: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    onTyping: (String) -> Unit,
) {
    var inputContentLength by rememberSaveable { mutableIntStateOf(0) }

    val inputState = rememberTextFieldState()

    LaunchedEffect(inputState) {
        snapshotFlow { inputState.text.toString().trim() }
            .collect { text ->
                onTyping(text)
            }
    }

    TextField(
        modifier = modifier.fillMaxWidth(),
        state = inputState,
        enabled = enabled,
        lineLimits = lineLimits,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = isError,
        placeholder = { Text(placeholder) },
        supportingText = if (supportingText != null || showCharacterCounter) {
            {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    supportingText?.invoke()

                    if (!showCharacterCounter || minimumCharacterCount == null) {
                        return@Row
                    }

                    CharacterCounter(
                        state = inputState,
                        minimumCharacterCount = minimumCharacterCount,
                        counterText = "$inputContentLength / $minimumCharacterCount min",
                        onTooShort = onTooShort,
                        onMinimumReached = onMinimumReached
                    ) {
                        inputContentLength = it
                    }
                }
            }
        } else null,
        trailingIcon = trailingIcon,
    )
}