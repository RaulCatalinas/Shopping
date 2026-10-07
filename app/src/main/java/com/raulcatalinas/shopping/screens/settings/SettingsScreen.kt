package com.raulcatalinas.shopping.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulcatalinas.shopping.screens.settings.enums.LanguageOptions
import com.raulcatalinas.shopping.screens.settings.enums.ThemeOptions
import com.raulcatalinas.shopping.screens.settings.viewmodels.SettingsViewModel
import com.raulcatalinas.shopping.shared.components.SegmentedButton

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SegmentedButton(
                options = ThemeOptions.entries.toList(),
                selectedOption = settings.theme,
                onOptionSelected = {
                    viewModel.setTheme(it)
                },
                label = { it.label }
            )

            SegmentedButton(
                options = LanguageOptions.entries.toList(),
                selectedOption = settings.language,
                onOptionSelected = {
                    viewModel.setLanguage(it)
                },
                label = { it.label }
            )
        }
    }
}