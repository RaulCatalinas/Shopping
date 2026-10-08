package com.raulcatalinas.shopping.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.raulcatalinas.shopping.backend.auth.viewmodels.AuthViewModel
import com.raulcatalinas.shopping.backend.profiles.viewModels.ProfileViewModel
import com.raulcatalinas.shopping.shared.components.ConfirmDialog
import com.raulcatalinas.shopping.shared.components.SectionHeader
import com.raulcatalinas.shopping.shared.extensions.verticalScrollbar
import com.raulcatalinas.shopping.shared.utils.showToast

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val scrollState = rememberScrollState()

    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        profileViewModel.fetchUserProfile()
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxWidth()
                .verticalScrollbar(scrollState)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (profileViewModel.username.isNotBlank()) "Hello, ${profileViewModel.username}!" else "Your Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            SectionHeader(title = "Profile details")

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = profileViewModel.username,
                enabled = !profileViewModel.isLoading,
                onValueChange = { profileViewModel.onUsernameChange(it) },
                label = { Text("Username") },
                placeholder = { Text("e.g. JohnDoe") },
                singleLine = true,
            )

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    profileViewModel.updateUsername { success ->
                        showToast(
                            context,
                            if (success) "Username updated successfully"
                            else "Failed to update username"
                        )
                    }
                },
                enabled = profileViewModel.username.isNotBlank() && !profileViewModel.isLoading
            ) {
                if (profileViewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    return@OutlinedButton
                }

                Text("Save changes")
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))


            SectionHeader(title = "Account")

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showSignOutDialog = true },
            ) {
                Text("Log out")
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showDeleteAccountDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Delete account")
            }
        }

        if (showSignOutDialog) {
            ConfirmDialog(
                title = "Log out",
                content = "Are you sure you want to log out?"
            ) {
                authViewModel.signOut { success ->
                    showToast(
                        context,
                        if (success) "Session closed successfully"
                        else "We were unable to log you out; please try again later"
                    )
                }
            }
        }

        if (showDeleteAccountDialog) {
            ConfirmDialog(
                title = "Delete account",
                content = "Are you sure you want to delete your account? This action cannot be undone."
            ) {
                authViewModel.deleteAccount { success ->
                    showToast(
                        context,
                        if (success) "Account deleted successfully"
                        else "We were unable to delete your account; please try again later"
                    )
                }
            }
        }
    }
}