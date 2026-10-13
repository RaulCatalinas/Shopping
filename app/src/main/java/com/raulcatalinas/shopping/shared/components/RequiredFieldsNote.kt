package com.raulcatalinas.shopping.shared.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.raulcatalinas.shopping.R

@Composable
fun RequiredFieldsNote(modifier: Modifier = Modifier) {
    Text(
        text = "* ${stringResource(R.string.all_fields_are_required)}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth()
    )
}