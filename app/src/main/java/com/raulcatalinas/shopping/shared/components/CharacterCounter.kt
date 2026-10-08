package com.raulcatalinas.shopping.shared.components

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

@Composable
fun CharacterCounter(
    state: TextFieldState,
    minimumCharacterCount: Int,
    counterText: String,
    modifier: Modifier = Modifier,
    onMinimumReached: (() -> Unit)? = null,
    onTooShort: (() -> Unit)? = null,
    validColor: Color = Color(0xFF34C759),
    invalidColor: Color = Color(0xFFFF3B30),
    onCharacterCountChanged: ((Int) -> Unit)? = null
) {
    val currentText = state.text.toString().trim()
    val characterCount = currentText.length
    val meetsMinimum = characterCount >= minimumCharacterCount
    val textColor = if (meetsMinimum) validColor else invalidColor

    var previousMeetsMinimum by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(characterCount) {
        onCharacterCountChanged?.invoke(characterCount)

        if (meetsMinimum == previousMeetsMinimum) return@LaunchedEffect

        previousMeetsMinimum = meetsMinimum

        if (meetsMinimum) {
            onMinimumReached?.invoke()

            return@LaunchedEffect
        }

        onTooShort?.invoke()
    }

    Text(
        text = counterText,
        style = TextStyle(
            fontSize = 11.sp,
            color = textColor
        ),
        modifier = modifier
    )
}