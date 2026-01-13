package com.saha.androidfm.views.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.saha.androidfm.ui.theme.secondaryTextColor
import com.saha.androidfm.utils.ext.singleClick
import com.saha.androidfm.utils.helpers.SuccessDialogSpec

/*@Composable
fun SuccessDialog(spec: SuccessDialogSpec, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = {
            onDismiss
        }, properties = DialogProperties(
            dismissOnBackPress = spec.cancellable, dismissOnClickOutside = spec.cancellable
        )
    ) {

        Card(
            modifier = Modifier.wrapContentWidth(),
            //.weight(1f, false),
            shape = MaterialTheme.shapes.small, colors = CardColors(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = Color.Black,
                disabledContentColor = MaterialTheme.colorScheme.background,
                disabledContainerColor = MaterialTheme.colorScheme.background
            )
        ) {

        }
    }
}*/

@Composable
fun IosSuccessDialog(
    spec: SuccessDialogSpec,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = {
            if (spec.cancellable) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = spec.cancellable,
            dismissOnClickOutside = spec.cancellable
        )
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            shadowElevation = 12.dp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.widthIn(min = 280.dp, max = 340.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Success",
                        tint = IosGreen,
                        modifier = Modifier.size(44.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = spec.title,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = spec.message,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }

                Divider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = secondaryTextColor)

                // iOS-style single action button (dismiss + action)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .singleClick {
                            spec.action.onClick()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = spec.action.text,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

private val IosGreen = Color(0xFF34C759)          // iOS system green
private val IosSeparator = Color(0x1F3C3C43)      // subtle separator