package com.raulcatalinas.shopping.shared.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun ConfirmDialog(
    title: String,
    content: String,
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
                onClick = {
                    showDialog = false
                    onConfirm()
                }
            ) {
                Text("Yes")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { showDialog = false }) {
                Text("No")
            }
        }
    )
}