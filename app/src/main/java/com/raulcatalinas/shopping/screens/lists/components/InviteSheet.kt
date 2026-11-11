package com.raulcatalinas.shopping.screens.lists.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.raulcatalinas.shopping.R
import com.raulcatalinas.shopping.shared.components.AppBottomSheet
import com.raulcatalinas.shopping.shared.components.SegmentedButton
import com.raulcatalinas.shopping.shared.enums.InviteDuration

@Composable
fun InviteSheet(
    listName: String,
    trigger: @Composable (open: () -> Unit) -> Unit,
    onCreateInvitation: (inviteDuration: InviteDuration, onResult: (link: String?) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val resources = LocalResources.current

    AppBottomSheet(
        trigger = trigger,
        title = stringResource(R.string.invite_title)
    ) { dismiss ->
        var duration by remember { mutableStateOf(InviteDuration.ONE_WEEK) }
        var isCreating by remember { mutableStateOf(false) }

        Text(
            text = stringResource(R.string.invite_description),
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = stringResource(R.string.invite_duration_label),
            style = MaterialTheme.typography.labelLarge
        )

        SegmentedButton(
            options = InviteDuration.entries.toList(),
            selectedOption = duration,
            onOptionSelected = { duration = it },
            label = { stringResource(it.labelRes) },
            enabled = !isCreating
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !isCreating,
            onClick = {
                isCreating = true

                onCreateInvitation(duration) { link ->
                    isCreating = false

                    if (link == null) return@onCreateInvitation

                    shareLink(
                        context = context,
                        message = resources.getString(
                            R.string.invite_share_message,
                            listName,
                            link
                        )
                    )

                    dismiss()
                }
            }
        ) {
            if (isCreating) {
                CircularProgressIndicator(
                    modifier = Modifier.width(20.dp),
                    strokeWidth = 2.dp
                )

                return@Button
            }

            Text(
                stringResource(
                    R.string.invite_share_btn
                )
            )
        }
    }
}

// Opens the Android share menu (WhatsApp, Telegram, messages...)
private fun shareLink(context: Context, message: String) {
    val intent =
        Intent(Intent.ACTION_SEND)
            .apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    message
                )
            }

    context.startActivity(
        Intent.createChooser(
            intent,
            null
        )
    )
}