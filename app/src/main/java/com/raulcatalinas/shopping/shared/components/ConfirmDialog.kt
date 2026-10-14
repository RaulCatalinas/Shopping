package com.raulcatalinas.shopping.shared.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.raulcatalinas.shopping.R

@Composable
fun ConfirmDialog(
    title: String,
    content: String,
    confirmText: String = stringResource(R.string.yes_btn),
    dismissText: String = stringResource(R.string.no_btn),
    isDestructiveAction: Boolean = false,
    onConfirm: () -> Unit,
    trigger: @Composable (open: () -> Unit) -> Unit
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    trigger { showDialog = true }

    if (!showDialog) return

    AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text(title) },
        text = { Text(content) },
        confirmButton = {
            Button(
                colors = confirmButtonColors(isDestructiveAction),
                onClick = {
                    showDialog = false
                    onConfirm()
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { showDialog = false }) {
                Text(dismissText)
            }
        }
    )
}

@Composable
private fun confirmButtonColors(isDestructiveAction: Boolean): ButtonColors {
    if (!isDestructiveAction) return ButtonDefaults.buttonColors()

    return ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError
    )
}