package com.raulcatalinas.shopping.shared.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    trigger: @Composable (open: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    skipPartiallyExpanded: Boolean = true,
    content: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit
) {
    var showSheet by rememberSaveable { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val scope = rememberCoroutineScope()

    trigger { showSheet = true }

    if (!showSheet) return

    // Hide first (animated) and only then remove the sheet from the composition
    val dismiss: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) showSheet = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = { showSheet = false },
        sheetState = sheetState
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                // Keeps the content above the keyboard
                .imePadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            content(dismiss)
        }
    }
}