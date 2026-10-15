package com.raulcatalinas.shopping.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.raulcatalinas.shopping.R
import com.raulcatalinas.shopping.shared.components.Budget
import com.raulcatalinas.shopping.shared.constants.MOBILE_PHONE_UNI_CODE
import com.raulcatalinas.shopping.shared.extensions.toEmoji
import com.raulcatalinas.shopping.shared.extensions.verticalScrollbar
import com.raulcatalinas.shopping.shared.types.ShoppingList

@Composable
fun HomeWithLists(
    paddingValues: PaddingValues,
    shoppingLists: List<ShoppingList>
) {
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
        items(shoppingLists) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { println("Navigating to detail...") }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(it.name)

                        Text(
                            if (it.pendingCount == 0) stringResource(
                                R.string.all_bought
                            )
                            else stringResource(
                                R.string.shopping_elements_remaining,
                                it.pendingCount,
                                it.totalCount
                            )
                        )
                    }

                    Budget(
                        "${MOBILE_PHONE_UNI_CODE.toEmoji()} ${it.memberIds.size + 1}"
                    )
                }
            }
        }
    }
}