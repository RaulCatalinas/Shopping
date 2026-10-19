package com.raulcatalinas.shopping.screens.lists.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.raulcatalinas.shopping.R
import com.raulcatalinas.shopping.shared.components.AppBottomSheet
import com.raulcatalinas.shopping.shared.components.Input

@Composable
fun EditListSheet(
    currentName: String,
    isSaving: Boolean,
    trigger: @Composable (open: () -> Unit) -> Unit,
    onSave: (name: String, onSaved: () -> Unit) -> Unit
) {
    AppBottomSheet(
        trigger = trigger,
        title = stringResource(R.string.rename_list_title)
    ) { dismiss ->
        var name by remember { mutableStateOf(currentName) }
        var minimumCharsReached by remember { mutableStateOf(false) }

        Input(
            autoFocus = true,
            enabled = !isSaving,
            initialValue = currentName,
            showCharacterCounter = true,
            minimumCharacterCount = 3,
            placeholder = stringResource(R.string.create_list_input_placeholder),
            onTooShort = { minimumCharsReached = false },
            onMinimumReached = { minimumCharsReached = true }
        ) {
            name = it
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = minimumCharsReached && name != currentName && !isSaving,
            onClick = { onSave(name, dismiss) }
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.width(20.dp),
                    strokeWidth = 2.dp
                )

                return@Button
            }

            Text(stringResource(R.string.save_btn))
        }
    }
}