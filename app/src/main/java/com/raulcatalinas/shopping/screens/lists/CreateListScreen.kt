package com.raulcatalinas.shopping.screens.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulcatalinas.shopping.R
import com.raulcatalinas.shopping.core.lists.viewmodels.ListsViewModel
import com.raulcatalinas.shopping.shared.components.AppScaffold
import com.raulcatalinas.shopping.shared.components.Input
import com.raulcatalinas.shopping.shared.components.RequiredFieldsNote
import com.raulcatalinas.shopping.shared.utils.LocalSnackbarController

@Composable
fun CreateListScreen(
    viewModel: ListsViewModel = hiltViewModel(),
    onListCreated: (listId: String) -> Unit,
) {
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    val snackbar = LocalSnackbarController.current
    val resources = LocalResources.current

    var listNameMinimumCharsReached by rememberSaveable { mutableStateOf(false) }
    var listName by remember { mutableStateOf("") }


    AppScaffold { paddingValues ->
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RequiredFieldsNote()

            Input(
                enabled = !isSaving,
                autoFocus = true,
                showCharacterCounter = true,
                minimumCharacterCount = 3,
                placeholder = stringResource(
                    R.string.create_list_input_placeholder
                ),
                onTooShort = { listNameMinimumCharsReached = false },
                onMinimumReached = { listNameMinimumCharsReached = true },
            ) {
                listName = it
            }

            Button(
                onClick = {
                    viewModel.createList(
                        listName = listName,
                        onCreated = { list -> onListCreated(list.id) },
                        onError = { errorRes -> snackbar.show(resources.getString(errorRes)) }
                    )
                },
                enabled = listNameMinimumCharsReached && !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )

                    return@Button
                }

                Text(stringResource(R.string.save_new_list_btn))
            }
        }
    }
}