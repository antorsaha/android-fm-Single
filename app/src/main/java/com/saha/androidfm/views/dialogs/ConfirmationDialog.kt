package com.saha.androidfm.views.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.saha.androidfm.ui.theme.primaryTextColor
import com.saha.androidfm.ui.theme.secondaryTextColor
import com.saha.androidfm.utils.ext.singleClick
import com.saha.androidfm.utils.helpers.ConfirmationDialogSpec

@Composable
fun IosConfirmationDialog(
    spec: ConfirmationDialogSpec,
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
                    Text(
                        text = spec.title,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = spec.description,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }

                Divider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = secondaryTextColor)

                // iOS-style two buttons side by side
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Negative button (left)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .singleClick {
                                spec.negative.onClick()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = spec.negative.text,
                            style = MaterialTheme.typography.titleMedium,
                            color = secondaryTextColor
                        )
                    }

                    // Vertical divider between buttons
                    Divider(
                        modifier = Modifier
                            .width(1.dp)
                            .height(48.dp),
                        color = secondaryTextColor
                    )

                    // Positive button (right)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .singleClick {
                                spec.positive.onClick()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = spec.positive.text,
                            style = MaterialTheme.typography.titleMedium,
                            color = primaryTextColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

private val IosBlue = Color(0xFF007AFF) // iOS system blue
private val IosRed = Color(0xFFFF3B30) // iOS system red

