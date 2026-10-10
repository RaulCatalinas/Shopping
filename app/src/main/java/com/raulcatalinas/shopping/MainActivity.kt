package com.raulcatalinas.shopping

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.raulcatalinas.shopping.backend.auth.viewmodels.AuthState
import com.raulcatalinas.shopping.backend.auth.viewmodels.AuthViewModel
import com.raulcatalinas.shopping.screens.auth.AuthScreen
import com.raulcatalinas.shopping.screens.auth.ResetPasswordScreen
import com.raulcatalinas.shopping.screens.home.HomeScreen
import com.raulcatalinas.shopping.screens.profile.ProfileScreen
import com.raulcatalinas.shopping.screens.settings.SettingsScreen
import com.raulcatalinas.shopping.screens.settings.viewmodels.SettingsViewModel
import com.raulcatalinas.shopping.ui.theme.ShoppingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var deepLinkRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        handleDeepLink(intent)

        val authViewModel: AuthViewModel by viewModels()

        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition {
            authViewModel.authState.value is AuthState.Loading
        }

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()

            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            val authState by authViewModel.authState.collectAsStateWithLifecycle()

            ShoppingTheme(
                theme = settings.theme
            ) {
                when (authState) {
                    AuthState.Loading -> {}
                    AuthState.Authenticated,
                    AuthState.Unauthenticated -> {
                        ShoppingApp(
                            authState = authState,
                            deepLinkRoute = deepLinkRoute,
                            onDeepLinkHandled = { deepLinkRoute = null }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data ?: return

        if (data.scheme == "shopping" && data.host == "reset-password") {
            deepLinkRoute = "RESET_PASSWORD"
        }
    }
}

@Composable
fun ShoppingApp(
    authState: AuthState,
    deepLinkRoute: String? = null,
    onDeepLinkHandled: () -> Unit = {},
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(deepLinkRoute) {
        deepLinkRoute?.let { route ->
            navController.navigate(route) {
                launchSingleTop = true
            }
            onDeepLinkHandled()
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                val isSelected = currentRoute == destination.name ||
                        (currentRoute == "AUTH" && destination == AppDestinations.HOME)

                val isItemEnabled = destination.enabled || authState is AuthState.Authenticated

                item(
                    enabled = isItemEnabled,
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label,
                            modifier = Modifier.size(destination.iconSize.dp)
                        )
                    },
                    label = { Text(destination.label) },
                    selected = isSelected,
                    onClick = {
                        if (
                            currentRoute == destination.name
                            || (
                                    currentRoute == "AUTH"
                                            && destination == AppDestinations.HOME
                                    )
                        ) {
                            return@item
                        }

                        if (
                            destination.requiresAuth
                            && authState is AuthState.Unauthenticated
                        ) {
                            navController.navigate("AUTH") {
                                launchSingleTop = true
                            }

                            return@item
                        }

                        navController.navigate(destination.name) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }

                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = if (authState is AuthState.Authenticated) {
                AppDestinations.HOME.name
            } else {
                "AUTH"
            }
        ) {
            composable("AUTH") { AuthScreen() }

            composable("RESET_PASSWORD") {
                ResetPasswordScreen(
                    onPasswordResetSuccess = {
                        navController.navigate("AUTH") {
                            popUpTo("RESET_PASSWORD") { inclusive = true }
                        }
                    }
                )
            }

            AppDestinations.entries.forEach { destination ->
                composable(destination.name) {
                    if (destination.requiresAuth && authState !is AuthState.Authenticated) {
                        LaunchedEffect(Unit) {
                            navController.navigate("AUTH") {
                                popUpTo(destination.name) { inclusive = true }
                            }
                        }

                        return@composable
                    }

                    destination.content()
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val iconSize: Int,
    val requiresAuth: Boolean,
    val enabled: Boolean = true,
    val content: @Composable () -> Unit,
) {
    HOME(
        label = "Home",
        icon = Icons.Default.Home,
        iconSize = 24,
        requiresAuth = true,
        content = { HomeScreen() }
    ),
    PROFILE(
        label = "Profile",
        icon = Icons.Default.Person,
        iconSize = 24,
        requiresAuth = true,
        enabled = false,
        content = { ProfileScreen() }
    ),
    SETTINGS(
        label = "Settings",
        icon = Icons.Default.Settings,
        iconSize = 24,
        requiresAuth = false,
        content = { SettingsScreen() }
    ),
}