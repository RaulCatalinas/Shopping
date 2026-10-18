package com.raulcatalinas.shopping.screens.lists.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.raulcatalinas.shopping.R
import com.raulcatalinas.shopping.shared.components.AppBottomSheet
import com.raulcatalinas.shopping.shared.components.Input
import com.raulcatalinas.shopping.shared.components.QuantityStepper

@Composable
fun AddItemSheet(
    trigger: @Composable (open: () -> Unit) -> Unit,
    isSaving: Boolean,
    onSave: (name: String, quantity: Int, onSaved: () -> Unit) -> Unit
) {
    AppBottomSheet(
        trigger = trigger,
        title = stringResource(R.string.add_item_title)
    ) { dismiss ->
        var name by remember { mutableStateOf("") }
        var quantity by rememberSaveable { mutableIntStateOf(1) }

        Input(
            autoFocus = true,
            enabled = !isSaving,
            placeholder = stringResource(R.string.item_name_placeholder)
        ) {
            name = it
        }

        QuantityStepper(
            label = stringResource(R.string.quantity_label),
            quantity = quantity,
            enabled = !isSaving,
            onChange = { quantity = it }
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotEmpty() && !isSaving,
            onClick = { onSave(name, quantity, dismiss) }
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