package com.raulcatalinas.shopping.shared.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import com.raulcatalinas.shopping.shared.utils.LocalSnackbarController

@Composable
fun AppScaffold(content: @Composable (PaddingValues) -> Unit) {
    val snackbar = LocalSnackbarController.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar.hostState) },
        content = content
    )
}