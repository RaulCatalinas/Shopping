package com.raulcatalinas.shopping.shared.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

// Tap and long press are handled in ONE place (the row). If the Checkbox kept its
// own click handler, it would consume the touch and a long press set on a parent
// would never be delivered.
@Composable
fun AppCheckbox(
    modifier: Modifier = Modifier,
    text: String? = null,
    textDecoration: TextDecoration? = null,
    initialValue: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    var isChecked by remember { mutableStateOf(initialValue) }

    Row(
        modifier = modifier.combinedClickable(
            role = Role.Checkbox,
            onClick = {
                isChecked = !isChecked
                onCheckedChange(isChecked)
            },
            onLongClick = onLongClick
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // onCheckedChange = null: the row above already handles the click
        Checkbox(
            checked = isChecked,
            onCheckedChange = null
        )

        if (text != null) {
            Text(
                text = text,
                textDecoration = textDecoration,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}