package com.raulcatalinas.shopping.screens.settings

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulcatalinas.shopping.R
import com.raulcatalinas.shopping.screens.settings.enums.LanguageOptions
import com.raulcatalinas.shopping.screens.settings.enums.ThemeOptions
import com.raulcatalinas.shopping.screens.settings.viewmodels.SettingsViewModel
import com.raulcatalinas.shopping.shared.components.SectionHeader
import com.raulcatalinas.shopping.shared.components.SegmentedButton
import com.raulcatalinas.shopping.shared.extensions.verticalScrollbar

@Composable
fun SettingsScreen(
    @SuppressLint("ContextCastToActivity")
    viewModel: SettingsViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
) {
    val scrollState = rememberScrollState()

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val themeOptions = ThemeOptions.entries.toList()
    val languageOptions = LanguageOptions.entries.toList()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScrollbar(scrollState)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionHeader(stringResource(R.string.theme))

            SegmentedButton(
                options = themeOptions,
                selectedOption = settings.theme,
                onOptionSelected = { newTheme ->
                    viewModel.setTheme(newTheme)
                },
                label = { stringResource(it.labelRes) }
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            SectionHeader(stringResource(R.string.language))

            SegmentedButton(
                options = languageOptions,
                selectedOption = settings.language,
                onOptionSelected = { newLanguage ->
                    viewModel.setLanguage(newLanguage)
                },
                label = { stringResource(it.labelRes) }
            )
        }
    }
}