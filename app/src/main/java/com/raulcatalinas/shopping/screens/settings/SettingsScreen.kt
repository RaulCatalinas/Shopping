package com.raulcatalinas.shopping.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen() {
    Scaffold {
        Column(Modifier.padding(it)) {
            Text(
                text = "Settings Screen",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontSize = 18.sp
            )
        }
    }
}