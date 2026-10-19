package com.raulcatalinas.shopping.screens.home.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raulcatalinas.shopping.R
import com.raulcatalinas.shopping.screens.lists.components.EditListSheet
import com.raulcatalinas.shopping.shared.components.Budget
import com.raulcatalinas.shopping.shared.components.ConfirmDialog
import com.raulcatalinas.shopping.shared.components.EditDeleteMenu
import com.raulcatalinas.shopping.shared.constants.MOBILE_PHONE_UNI_CODE
import com.raulcatalinas.shopping.shared.extensions.toEmoji
import com.raulcatalinas.shopping.shared.extensions.verticalScrollbar
import com.raulcatalinas.shopping.shared.types.ShoppingList

@Composable
fun HomeWithLists(
    paddingValues: PaddingValues,
    shoppingLists: List<ShoppingList>,
    isSaving: Boolean,
    onListClick: (String) -> Unit,
    onListRename: (listId: String, name: String, onSaved: () -> Unit) -> Unit,
    onListDelete: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .padding(paddingValues)
            .verticalScrollbar(listState)
            .fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 16.dp,
            end = 20.dp,
            bottom = 88.dp
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(shoppingLists, key = { it.id }) { list ->
            val subtitle = when {
                list.totalCount == 0 -> stringResource(R.string.list_card_empty)
                list.pendingCount == 0 -> stringResource(R.string.all_bought)
                list.pendingCount == list.totalCount -> stringResource(
                    R.string.list_card_none_bought
                )

                else -> stringResource(
                    R.string.shopping_elements_remaining,
                    list.pendingCount,
                    list.totalCount
                )
            }

            ConfirmDialog(
                title = stringResource(R.string.delete_list),
                content = stringResource(
                    R.string.delete_list_confirm_dialog_body,
                    list.name
                ),
                confirmText = stringResource(R.string.delete_btn),
                dismissText = stringResource(R.string.keep_list_btn),
                isDestructiveAction = true,
                onConfirm = { onListDelete(list.id) },
                trigger = { openDelete ->
                    EditListSheet(
                        currentName = list.name,
                        isSaving = isSaving,
                        onSave = { name, onSaved -> onListRename(list.id, name, onSaved) },
                        trigger = { openRename ->
                            var menuExpanded by remember { mutableStateOf(false) }

                            // The Box is the anchor the menu appears next to
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Card(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            // Makes the ripple follow the rounded corners
                                            .clip(CardDefaults.shape)
                                            .combinedClickable(
                                                onClick = { onListClick(list.id) },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(
                                                        HapticFeedbackType.LongPress
                                                    )
                                                    menuExpanded = true
                                                },
                                                // TalkBack announces it, since a long
                                                // press is otherwise invisible
                                                onLongClickLabel =
                                                    stringResource(R.string.more_options)
                                            )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // weight(1f): takes the space left after the badge,
                                        // so the badge is never pushed out of the card
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = list.name,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.titleMedium,
                                            )

                                            Text(
                                                text = subtitle,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }

                                        Budget(
                                            "${MOBILE_PHONE_UNI_CODE.toEmoji()} ${list.memberIds.size + 1}"
                                        )
                                    }
                                }

                                EditDeleteMenu(
                                    expanded = menuExpanded,
                                    onDismiss = { menuExpanded = false },
                                    editText = stringResource(R.string.rename_btn),
                                    onEdit = openRename,
                                    onDelete = openDelete
                                )
                            }
                        }
                    )
                }
            )
        }
    }
}