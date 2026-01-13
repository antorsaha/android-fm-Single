package com.saha.androidfm.views.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saha.androidfm.R

@Composable
fun MyTopBar(
    onBackClick: (() -> Unit)? = null,
    showBackButton: Boolean = false,
    text: String,
    showProButton: Boolean = true,
    onProButtonClick: (() -> Unit)? = null
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        if (showBackButton) {
            IconButton(onClick = {
                onBackClick?.invoke()
            }) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "BackButton",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            WidthGap(16.dp)
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        if (showProButton) {

            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier
                .clickable { onProButtonClick?.invoke() }
                .background(
                    color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp)
                )
                .padding(
                    vertical = 4.dp, horizontal = 8.dp
                ), verticalAlignment = Alignment.CenterVertically

            ) {

                Image(
                    painter = painterResource(id = R.drawable.ic_pro),
                    contentDescription = "arrow",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(16.dp)
                )

                WidthGap(4.dp)

                Text(
                    text = stringResource(R.string.pro),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White, fontWeight = FontWeight(600)
                    )
                )
            }
        }
    }


}