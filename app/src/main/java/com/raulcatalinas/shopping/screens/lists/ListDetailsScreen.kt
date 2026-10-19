package com.raulcatalinas.shopping.screens.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulcatalinas.shopping.R
import com.raulcatalinas.shopping.core.lists.viewmodels.ListDetailUiState
import com.raulcatalinas.shopping.core.lists.viewmodels.ListsViewModel
import com.raulcatalinas.shopping.screens.lists.components.ItemFormSheet
import com.raulcatalinas.shopping.shared.components.AppCheckbox
import com.raulcatalinas.shopping.shared.components.AppScaffold
import com.raulcatalinas.shopping.shared.components.Budget
import com.raulcatalinas.shopping.shared.components.ConfirmDialog
import com.raulcatalinas.shopping.shared.components.EditDeleteMenu
import com.raulcatalinas.shopping.shared.components.StateMessage
import com.raulcatalinas.shopping.shared.components.StickySectionHeader
import com.raulcatalinas.shopping.shared.extensions.verticalScrollbar
import com.raulcatalinas.shopping.shared.types.ShoppingElement
import com.raulcatalinas.shopping.shared.types.ShoppingList
import com.raulcatalinas.shopping.shared.utils.LocalSnackbarController

@Composable
fun ListDetailsRoute(
    listId: String,
    onBack: () -> Unit,
    listsViewModel: ListsViewModel = hiltViewModel(),
) {
    val state by listsViewModel.detailState.collectAsStateWithLifecycle()
    val isSaving by listsViewModel.isSaving.collectAsStateWithLifecycle()

    val snackbar = LocalSnackbarController.current
    val resources = LocalResources.current

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

        ListDetailUiState.NotFound -> AppScaffold { paddingValues ->
            StateMessage(
                paddingValues = paddingValues,
                message = stringResource(R.string.list_not_found),
                actionText = stringResource(R.string.back_btn),
                onAction = onBack
            )
        }

        ListDetailUiState.Error -> AppScaffold { paddingValues ->
            StateMessage(
                paddingValues = paddingValues,
                message = stringResource(R.string.list_load_error),
                actionText = stringResource(R.string.retry_btn),
                onAction = { listsViewModel.loadListById(listId) }
            )
        }

        is ListDetailUiState.Success -> ListDetailsScreen(
            list = current.list,
            elements = current.elements,
            isSaving = isSaving,
            onToggleElement = { elementId, checked ->
                listsViewModel.setElementChecked(elementId, checked) { errorRes ->
                    snackbar.show(resources.getString(errorRes))
                }
            },
            onAddItem = { name, quantity, onSaved ->
                listsViewModel.createShoppingElement(
                    elementName = name,
                    quantity = quantity,
                    listId = listId
                ) { errorRes ->
                    if (errorRes != null) {
                        snackbar.show(resources.getString(errorRes))

                        return@createShoppingElement
                    }

                    listsViewModel.loadListById(listId)
                    onSaved()
                }
            },
            onEditItem = { elementId, name, quantity, onSaved ->
                listsViewModel.updateShoppingElement(
                    elementId = elementId,
                    elementName = name,
                    quantity = quantity
                ) { errorRes ->
                    // On failure the sheet stays open with what the user typed
                    if (errorRes != null) {
                        snackbar.show(resources.getString(errorRes))

                        return@updateShoppingElement
                    }

                    onSaved()
                }
            },
            onDeleteItem = { elementId ->
                listsViewModel.deleteShoppingElement(elementId) { errorRes ->
                    snackbar.show(resources.getString(errorRes))
                }
            }
        )
    }
}

@Composable
private fun ListDetailsScreen(
    list: ShoppingList,
    elements: List<ShoppingElement>,
    isSaving: Boolean,
    onToggleElement: (elementId: String, checked: Boolean) -> Unit,
    onAddItem: (name: String, quantity: Int, onSaved: () -> Unit) -> Unit,
    onEditItem: (elementId: String, name: String, quantity: Int, onSaved: () -> Unit) -> Unit,
    onDeleteItem: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current

    val (
        boughtElements,
        pendingElements
    ) = remember(elements) {
        elements.partition { it.isChecked }
    }

    AppScaffold { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ItemFormSheet(
                title = stringResource(R.string.add_item_title),
                isSaving = isSaving,
                onSave = onAddItem,
                trigger = { open ->
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = open
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )

                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))

                        Text("${stringResource(R.string.add_item)}...")
                    }
                }
            )

            LazyColumn(
                state = listState,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScrollbar(listState),
                // Horizontal padding is already applied by the parent Column
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = list.name,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                if (elements.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.empty_list),
                            textAlign = TextAlign.Center,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp)
                        )
                    }

                    return@LazyColumn
                }

                stickyHeader(key = "pending_header") {
                    StickySectionHeader(
                        stringResource(
                            R.string.items_still_to_be_bought,
                            pendingElements.size
                        )
                    )
                }

                items(pendingElements, key = { it.id }) { element ->
                    ElementRow(
                        element = element,
                        isSaving = isSaving,
                        haptic = haptic,
                        onToggle = { onToggleElement(element.id, it) },
                        onEditItem = onEditItem,
                        onDeleteItem = onDeleteItem
                    )
                }

                stickyHeader(key = "bought_header") {
                    StickySectionHeader(
                        stringResource(
                            R.string.items_purchased,
                            boughtElements.size
                        )
                    )
                }

                items(boughtElements, key = { it.id }) { element ->
                    ElementRow(
                        element = element,
                        isSaving = isSaving,
                        haptic = haptic,
                        onToggle = { onToggleElement(element.id, it) },
                        onEditItem = onEditItem,
                        onDeleteItem = onDeleteItem
                    )
                }
            }
        }
    }
}

// A long press opens a small menu with "Edit" and "Delete". Each action opens its own
// sheet or dialog, which manage their own state through their `trigger`.
@Composable
private fun ElementRow(
    element: ShoppingElement,
    isSaving: Boolean,
    haptic: HapticFeedback,
    onToggle: (Boolean) -> Unit,
    onEditItem: (elementId: String, name: String, quantity: Int, onSaved: () -> Unit) -> Unit,
    onDeleteItem: (String) -> Unit
) {
    ConfirmDialog(
        title = stringResource(R.string.delete_element),
        content = stringResource(
            R.string.delete_list_confirm_dialog_body,
            element.name
        ),
        onConfirm = { onDeleteItem(element.id) },
        confirmText = stringResource(R.string.delete_btn),
        dismissText = stringResource(R.string.keep_element_btn),
        isDestructiveAction = true,
        trigger = { openDelete ->
            ItemFormSheet(
                title = stringResource(R.string.edit_item_title),
                isSaving = isSaving,
                initialName = element.name,
                initialQuantity = element.quantity,
                onSave = { name, quantity, onSaved ->
                    onEditItem(
                        element.id,
                        name,
                        quantity,
                        onSaved
                    )
                },
                trigger = { openEdit ->
                    var menuExpanded by remember { mutableStateOf(false) }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AppCheckbox(
                                    checked = element.isChecked,
                                    text = element.name,
                                    textDecoration =
                                        if (element.isChecked) TextDecoration.LineThrough
                                        else null,
                                    modifier = Modifier.weight(1f),
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        menuExpanded = true
                                    }
                                ) {
                                    onToggle(it)
                                }

                                if (element.quantity >= 2) Budget("x${element.quantity}")
                            }

                            HorizontalDivider()
                        }

                        EditDeleteMenu(
                            expanded = menuExpanded,
                            onDismiss = { menuExpanded = false },
                            onEdit = openEdit,
                            onDelete = openDelete
                        )
                    }
                }
            )
        }
    )
}