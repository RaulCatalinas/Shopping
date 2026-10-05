package com.raulcatalinas.shopping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.raulcatalinas.shopping.screens.home.HomeScreen
import com.raulcatalinas.shopping.screens.settings.SettingsScreen
import com.raulcatalinas.shopping.screens.settings.viewmodels.SettingsViewModel
import com.raulcatalinas.shopping.ui.theme.ShoppingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: SettingsViewModel = hiltViewModel()

            ShoppingTheme(theme = viewModel.settings.theme) {
                ShoppingApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun ShoppingApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label,
                            modifier = Modifier.size(it.iconSize.dp)
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        currentDestination.screen()
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val iconSize: Int,
    val screen: @Composable () -> Unit
) {
    HOME(
        "Home",
        Icons.Default.Home,
        24,
        { HomeScreen() }
    ),
    SETTINGS(
        "Settings",
        Icons.Default.Settings,
        24,
        { SettingsScreen() }
    ),
}