package com.saha.androidfm.views.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingItem(
    modifier: Modifier = Modifier, text: String, icon: @Composable () -> Unit, onClick: () -> Unit
) {

    Row(
        modifier = modifier.clickable { onClick.invoke() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        /*Image(
            painter = icon,
            contentDescription = "Rays",
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )*/

        icon()

        WidthGap(16.dp)

        Text(
            text = text, style = MaterialTheme.typography.bodyLarge
        )


    }

}