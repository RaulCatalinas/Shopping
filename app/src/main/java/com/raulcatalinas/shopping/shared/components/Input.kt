package com.raulcatalinas.shopping.shared.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun Input(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    isOutlined: Boolean = false,
    showCharacterCounter: Boolean = false,
    autoFocus: Boolean = false,
    minimumCharacterCount: Int? = null,
    clearSignal: Int = 0,
    placeholder: String,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    onTooShort: (() -> Unit)? = null,
    onMinimumReached: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    initialValue: String = "",
    onTyping: (String) -> Unit,
) {
    var inputContentLength by rememberSaveable { mutableIntStateOf(0) }
    var handledSignal by remember { mutableIntStateOf(clearSignal) }
    val focusRequester = remember { FocusRequester() }

    val state = rememberTextFieldState(initialValue)

    if (autoFocus) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }

    LaunchedEffect(clearSignal) {
        if (clearSignal != handledSignal) {
            state.clearText()
            handledSignal = clearSignal
        }
    }

    LaunchedEffect(state) {
        snapshotFlow { state.text.toString().trim() }
            .collect { text ->
                onTyping(text)
            }
    }

    val placeholderContent: @Composable () -> Unit = { Text(placeholder) }

    val supportingContent: (@Composable () -> Unit)? =
        if (supportingText != null || showCharacterCounter) {
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
                        state = state,
                        minimumCharacterCount = minimumCharacterCount,
                        counterText = "$inputContentLength / $minimumCharacterCount min",
                        onTooShort = onTooShort,
                        onMinimumReached = onMinimumReached
                    ) {
                        inputContentLength = it
                    }
                }
            }
        } else null

    val keyboardOptions = KeyboardOptions(keyboardType = keyboardType)

    if (isOutlined) {
        OutlinedTextField(
            modifier =
                modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            state = state,
            enabled = enabled,
            lineLimits = lineLimits,
            keyboardOptions = keyboardOptions,
            isError = isError,
            placeholder = placeholderContent,
            supportingText = supportingContent,
            trailingIcon = trailingIcon,
        )

        return
    }

    TextField(
        modifier =
            modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        state = state,
        enabled = enabled,
        lineLimits = lineLimits,
        keyboardOptions = keyboardOptions,
        isError = isError,
        placeholder = placeholderContent,
        supportingText = supportingContent,
        trailingIcon = trailingIcon,
    )
}