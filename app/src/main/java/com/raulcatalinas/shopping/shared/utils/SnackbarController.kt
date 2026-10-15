package com.raulcatalinas.shopping.shared.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SnackbarController(
    val hostState: SnackbarHostState,
    private val scope: CoroutineScope
) {
    fun show(message: String) {
        scope.launch {
            hostState.currentSnackbarData?.dismiss()

            hostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long,
                withDismissAction = true
            )
        }
    }
}

val LocalSnackbarController = staticCompositionLocalOf<SnackbarController> {
    error("SnackbarController not provided")
}