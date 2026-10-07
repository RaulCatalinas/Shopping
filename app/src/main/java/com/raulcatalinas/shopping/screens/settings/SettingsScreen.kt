package com.raulcatalinas.shopping.screens.settings

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulcatalinas.shopping.screens.settings.enums.LanguageOptions
import com.raulcatalinas.shopping.screens.settings.enums.ThemeOptions
import com.raulcatalinas.shopping.screens.settings.viewmodels.SettingsViewModel
import com.raulcatalinas.shopping.shared.components.SegmentedButton

@Composable
fun SettingsScreen(
    @SuppressLint("ContextCastToActivity")
    viewModel: SettingsViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val themeOptions = ThemeOptions.entries.toList()
    val languageOptions = LanguageOptions.entries.toList()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SegmentedButton(
                options = themeOptions,
                selectedOption = settings.theme,
                onOptionSelected = { newTheme ->
                    viewModel.setTheme(newTheme)
                },
                label = { it.label }
            )

            SegmentedButton(
                options = languageOptions,
                selectedOption = settings.language,
                onOptionSelected = { newLanguage ->
                    viewModel.setLanguage(newLanguage)
                },
                label = { it.label }
            )
        }
    }
}