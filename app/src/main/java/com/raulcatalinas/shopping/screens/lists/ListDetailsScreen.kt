package com.raulcatalinas.shopping.screens.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulcatalinas.shopping.R
import com.raulcatalinas.shopping.core.lists.viewmodels.ListDetailUiState
import com.raulcatalinas.shopping.core.lists.viewmodels.ListsViewModel
import com.raulcatalinas.shopping.shared.components.AppScaffold
import com.raulcatalinas.shopping.shared.components.Budget
import com.raulcatalinas.shopping.shared.components.SectionHeader
import com.raulcatalinas.shopping.shared.extensions.verticalScrollbar
import com.raulcatalinas.shopping.shared.types.ShoppingElement
import com.raulcatalinas.shopping.shared.types.ShoppingList

@Composable
fun ListDetailsRoute(
    listId: String,
    listsViewModel: ListsViewModel = hiltViewModel()
) {
    val state by listsViewModel.detailState.collectAsStateWithLifecycle()

    LaunchedEffect(listId) { listsViewModel.loadListById(listId) }

    when (val current = state) {
        ListDetailUiState.Loading -> AppScaffold { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // TODO: NotFound and Error screens (message + back / retry button)
        ListDetailUiState.NotFound -> {}
        ListDetailUiState.Error -> {}

        is ListDetailUiState.Success -> ListDetailsScreen(
            list = current.list,
            elements = current.elements
        )
    }
}

@Composable
private fun ListDetailsScreen(
    list: ShoppingList,
    elements: List<ShoppingElement>
) {
    val listState = rememberLazyListState()

    val pendingElements = elements.filter { !it.isChecked }
    val boughtElements = elements.filter { it.isChecked }

    AppScaffold { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
                .verticalScrollbar(listState),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = list.name,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            item {
                SectionHeader(
                    stringResource(
                        R.string.items_still_to_be_bought,
                        pendingElements.size
                    )
                )
            }

            items(pendingElements) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // TODO: add checkbox to alternate estate between isChecked and its not
                    Text(it.name)

                    if (it.quantity >= 2) Budget("x${it.quantity}")

                    HorizontalDivider()
                }
            }

            item {
                SectionHeader(
                    stringResource(
                        R.string.items_purchased,
                        boughtElements.size
                    )
                )
            }

            items(boughtElements) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // TODO: add checkbox to alternate estate between isChecked and its not
                    Text(
                        it.name,
                        textDecoration = TextDecoration.LineThrough
                    )

                    if (it.quantity >= 2) Budget("x${it.quantity}")

                    HorizontalDivider()
                }
            }
        }
    }
}