package com.raulcatalinas.shopping.screens.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.raulcatalinas.shopping.R
import com.raulcatalinas.shopping.shared.components.SectionHeader
import com.raulcatalinas.shopping.shared.extensions.verticalScrollbar

@Composable
fun HomeWithoutLists(paddingValues: PaddingValues, scrollState: ScrollState) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .verticalScrollbar(scrollState)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_icon),
            contentDescription = "App logo",
            modifier = Modifier.size(120.dp)
        )

        SectionHeader(stringResource(R.string.home_without_lists_header))

        Text(
            textAlign = TextAlign.Center,
            text = stringResource(R.string.home_without_lists_subtitle)
        )

        Button(
            onClick = { println("Creating new list...") }
        ) {
            Text(stringResource(R.string.create_new_list_btn))
        }

        OutlinedButton(
            onClick = { println("Joining a list...") }
        ) {
            Text(stringResource(R.string.join_a_list_btn))
        }
    }
}