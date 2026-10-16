package com.raulcatalinas.shopping

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.raulcatalinas.shopping.backend.auth.viewmodels.AuthState
import com.raulcatalinas.shopping.backend.auth.viewmodels.AuthViewModel
import com.raulcatalinas.shopping.screens.auth.AuthScreen
import com.raulcatalinas.shopping.screens.auth.ResetPasswordScreen
import com.raulcatalinas.shopping.screens.home.HomeScreen
import com.raulcatalinas.shopping.screens.lists.CreateListScreen
import com.raulcatalinas.shopping.screens.lists.ListDetailsRoute
import com.raulcatalinas.shopping.screens.profile.ProfileScreen
import com.raulcatalinas.shopping.screens.settings.SettingsScreen
import com.raulcatalinas.shopping.screens.settings.viewmodels.SettingsViewModel
import com.raulcatalinas.shopping.shared.constants.CREATE_LIST_ROUTE
import com.raulcatalinas.shopping.shared.constants.LIST_DETAIL_ROUTE
import com.raulcatalinas.shopping.shared.utils.LocalSnackbarController
import com.raulcatalinas.shopping.shared.utils.SnackbarController
import com.raulcatalinas.shopping.ui.theme.ShoppingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val snackbarHostState = SnackbarHostState()
    private val snackbarController by lazy {
        SnackbarController(snackbarHostState, lifecycleScope)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) handlePasswordResetIntent(intent)

        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition {
            authViewModel.authState.value is AuthState.Loading
        }

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()

            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            val authState by authViewModel.authState.collectAsStateWithLifecycle()
            CompositionLocalProvider(LocalSnackbarController provides snackbarController) {
                ShoppingTheme(
                    theme = settings.theme
                ) {
                    when (authState) {
                        AuthState.Loading -> {}

                        AuthState.PasswordRecovery -> ResetPasswordScreen(
                            onPasswordResetSuccess = {
                                authViewModel.finishRecovery(signOut = true, context = this)
                            }
                        )

                        AuthState.Authenticated,
                        AuthState.Unauthenticated -> {
                            ShoppingApp(authState = authState)
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePasswordResetIntent(intent)
    }

    private fun handlePasswordResetIntent(intent: Intent?) {
        val data = intent?.data ?: return

        if (
            intent.action != Intent.ACTION_VIEW
            || data.scheme != "shopping"
            || data.host != "reset-password"
        ) return

        authViewModel.handleDeepLink(Intent(intent)) { success, errorMessage ->
            if (!success) snackbarController.show(errorMessage!!)
        }

        intent.data = null
        intent.action = null
    }
}

@Composable
fun ShoppingApp(
    authState: AuthState,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                val isSelected =
                    currentRoute == destination.name
                            || currentRoute in destination.relatedRoutes

                val isItemEnabled = destination.enabled || authState is AuthState.Authenticated

                item(
                    enabled = isItemEnabled,
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = stringResource(destination.labelRes),
                            modifier = Modifier.size(destination.iconSize.dp)
                        )
                    },
                    label = { Text(stringResource(destination.labelRes)) },
                    selected = isSelected,
                    onClick = {
                        if (isSelected) return@item

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

            composable(CREATE_LIST_ROUTE) {
                CreateListScreen()
            }

            composable(
                route = LIST_DETAIL_ROUTE,
                arguments = listOf(navArgument("listId") { type = NavType.StringType })
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getString("listId")
                    ?: return@composable

                ListDetailsRoute(listId = listId)
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

                    destination.content(navController)
                }
            }
        }
    }
}

enum class AppDestinations(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val iconSize: Int,
    val requiresAuth: Boolean,
    val enabled: Boolean = true,
    val relatedRoutes: Set<String> = emptySet(),
    val content: @Composable (NavController) -> Unit,
) {
    HOME(
        labelRes = R.string.home_route,
        icon = Icons.Default.Home,
        iconSize = 24,
        requiresAuth = true,
        relatedRoutes = setOf("AUTH", CREATE_LIST_ROUTE, LIST_DETAIL_ROUTE),
        content = {
            HomeScreen(
                onCreateList = {
                    it.navigate(CREATE_LIST_ROUTE) { launchSingleTop = true }
                },
                onJoinList = { println("Joining to list...") },
                onListClick = { id ->
                    it.navigate("LIST/$id") { launchSingleTop = true }
                }
            )
        }
    ),
    PROFILE(
        labelRes = R.string.profile_route,
        icon = Icons.Default.Person,
        iconSize = 24,
        requiresAuth = true,
        enabled = false,
        content = { ProfileScreen() }
    ),
    SETTINGS(
        labelRes = R.string.settings_route,
        icon = Icons.Default.Settings,
        iconSize = 24,
        requiresAuth = false,
        content = { SettingsScreen() }
    ),
}