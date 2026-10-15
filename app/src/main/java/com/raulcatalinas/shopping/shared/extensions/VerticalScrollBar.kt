package com.raulcatalinas.shopping.shared.extensions

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
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
    paddingEnd: Dp = (-8).dp
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

// Scrollbar for LazyColumn. A lazy list only measures the items on screen, so the
// total height is estimated from the average height of the visible items.
// It is exact when all items have the same height and close enough otherwise.
fun Modifier.verticalScrollbar(
    state: LazyListState,
    width: Dp = 4.dp,
    color: Color = Color.Gray.copy(alpha = 0.5f),
    paddingEnd: Dp = 4.dp,
    minThumbHeight: Dp = 24.dp
): Modifier {
    return drawWithContent {
        drawContent()

        val layoutInfo = state.layoutInfo
        val visibleItems = layoutInfo.visibleItemsInfo
        val totalItems = layoutInfo.totalItemsCount

        if (visibleItems.isEmpty() || totalItems == 0) return@drawWithContent

        // Average item height, including the spacing between items
        val averageItemSize =
            visibleItems.sumOf { it.size }.toFloat() / visibleItems.size +
                    layoutInfo.mainAxisItemSpacing

        val viewportHeight = size.height
        val totalContentHeight = averageItemSize * totalItems

        // Everything fits on screen: no scrollbar needed
        if (totalContentHeight <= viewportHeight) return@drawWithContent

        val scrolled =
            state.firstVisibleItemIndex * averageItemSize + state.firstVisibleItemScrollOffset
        val maxScroll = totalContentHeight - viewportHeight

        val thumbHeight = (viewportHeight / totalContentHeight * viewportHeight)
            .coerceAtLeast(minThumbHeight.toPx())
        val thumbOffset = (scrolled / maxScroll).coerceIn(0f, 1f) * (viewportHeight - thumbHeight)

        val widthPx = width.toPx()
        val radius = widthPx / 2f

        drawRoundRect(
            color = color,
            topLeft = Offset(x = size.width - widthPx - paddingEnd.toPx(), y = thumbOffset),
            size = Size(width = widthPx, height = thumbHeight),
            cornerRadius = CornerRadius(x = radius, y = radius)
        )
    }
}