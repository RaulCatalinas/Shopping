package com.raulcatalinas.shopping.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.raulcatalinas.shopping.screens.settings.enums.LanguageOptions
import com.raulcatalinas.shopping.screens.settings.enums.ThemeOptions
import com.raulcatalinas.shopping.screens.settings.viewmodels.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings = viewModel.settings
    val themeOptions = ThemeOptions.entries.toList()
    val languageOptions = LanguageOptions.entries.toList()

    Scaffold {
        Column(
            modifier = Modifier.padding(it),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.padding(16.dp)
            ) {
                themeOptions.forEachIndexed { index, themeOption ->
                    SegmentedButton(
                        selected = settings.theme == themeOption,
                        onClick = {
                            viewModel.setTheme(themeOption)
                        },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = themeOptions.size
                        )
                    ) {
                        Text(
                            text = themeOption.label,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.padding(16.dp)
            ) {
                languageOptions.forEachIndexed { index, languageOption ->
                    SegmentedButton(
                        selected = settings.language == languageOption,
                        onClick = {
                            viewModel.setLanguage(languageOption)
                        },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = languageOptions.size
                        )
                    ) {
                        Text(
                            text = languageOption.label,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}