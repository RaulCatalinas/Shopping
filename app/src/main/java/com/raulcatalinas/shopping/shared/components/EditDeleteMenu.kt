package com.raulcatalinas.shopping.shared.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.raulcatalinas.shopping.R

@Composable
fun EditDeleteMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    editText: String = stringResource(R.string.edit_btn)
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text(editText) },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
            onClick = {
                onDismiss()
                onEdit()
            }
        )

        DropdownMenuItem(
            text = { Text(stringResource(R.string.delete_btn)) },
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
            onClick = {
                onDismiss()
                onDelete()
            }
        )
    }
}