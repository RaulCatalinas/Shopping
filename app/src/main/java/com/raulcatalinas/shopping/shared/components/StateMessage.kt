package com.raulcatalinas.shopping.shared.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun StateMessage(
    paddingValues: PaddingValues,
    message: String,
    actionText: String,
    onAction: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(text = message, textAlign = TextAlign.Center)

        Button(onClick = onAction) { Text(actionText) }
    }
}