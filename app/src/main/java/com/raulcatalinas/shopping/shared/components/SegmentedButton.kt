package com.raulcatalinas.shopping.shared.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun <T> SegmentedButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    label: @Composable (T) -> String
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = selectedOption == option,
                enabled = enabled,
                onClick = {
                    onOptionSelected(option)
                },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                )
            ) {
                Text(
                    text = label(option),
                    fontSize = 16.sp
                )
            }
        }
    }
}