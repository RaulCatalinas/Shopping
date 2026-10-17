package com.raulcatalinas.shopping.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulcatalinas.shopping.R
import com.raulcatalinas.shopping.core.lists.viewmodels.ListsUiState
import com.raulcatalinas.shopping.core.lists.viewmodels.ListsViewModel
import com.raulcatalinas.shopping.screens.home.components.HomeWithLists
import com.raulcatalinas.shopping.screens.home.components.HomeWithoutLists
import com.raulcatalinas.shopping.shared.components.AppScaffold
import com.raulcatalinas.shopping.shared.utils.LocalSnackbarController

@Composable
fun HomeScreen(
    listsViewModel: ListsViewModel = hiltViewModel(),
    onCreateList: () -> Unit,
    onJoinList: () -> Unit,
    onListClick: (String) -> Unit,
) {
    val state by listsViewModel.uiState.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    val snackbar = LocalSnackbarController.current
    val resources = LocalResources.current

    val showFloatingActionButton =
        (state as? ListsUiState.Success)
            ?.lists
            ?.isNotEmpty() == true

    LaunchedEffect(Unit) { listsViewModel.loadLists() }

    AppScaffold(
        floatingActionButton = {
            if (showFloatingActionButton) {
                FloatingActionButton(onClick = onCreateList) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(
                            R.string.create_new_list_btn
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        when (val current = state) {
            ListsUiState.Loading -> HomeLoading(paddingValues)

            ListsUiState.Error -> HomeLoadError(
                paddingValues = paddingValues,
                onRetry = listsViewModel::loadLists
            )

            is ListsUiState.Success -> {
                if (current.lists.isEmpty()) {
                    HomeWithoutLists(
                        paddingValues = paddingValues,
                        scrollState = scrollState,
                        onCreateList = onCreateList,
                        onJoinList = onJoinList
                    )
                } else {
                    HomeWithLists(
                        paddingValues = paddingValues,
                        shoppingLists = current.lists,
                        onListClick = onListClick,
                        onListDelete = { id ->
                            listsViewModel.deleteList(id) { errorRes ->
                                snackbar.show(resources.getString(errorRes))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeLoading(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun HomeLoadError(
    paddingValues: PaddingValues,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(stringResource(R.string.lists_load_error))

        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry_btn))
        }
    }
}