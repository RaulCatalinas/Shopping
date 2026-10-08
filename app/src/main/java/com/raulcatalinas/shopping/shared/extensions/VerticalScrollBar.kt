package com.raulcatalinas.shopping.shared.extensions

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.verticalScrollbar(
    state: ScrollState,
    width: Dp = 4.dp,
    color: Color = Color.Gray.copy(alpha = 0.5f),
    paddingEnd: Dp = (-30).dp
): Modifier {
    return drawWithContent {
        drawContent()

        if (state.maxValue <= 0) return@drawWithContent

        val widthPx = width.toPx()
        val paddingEndPx = paddingEnd.toPx()

        val visibleHeight = size.height
        val totalHeight = state.maxValue + visibleHeight

        val thumbHeight = (visibleHeight / totalHeight) * visibleHeight
        val thumbOffset = (state.value.toFloat() / state.maxValue) * (visibleHeight - thumbHeight)

        val radius = widthPx / 2f

        drawRoundRect(
            color = color,
            topLeft = Offset(x = size.width - widthPx - paddingEndPx, y = thumbOffset),
            size = Size(width = widthPx, height = thumbHeight),
            cornerRadius = CornerRadius(x = radius, y = radius)
        )
    }
}