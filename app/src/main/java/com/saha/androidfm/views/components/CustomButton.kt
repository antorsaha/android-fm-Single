package com.saha.androidfm.views.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.saha.androidfm.ui.theme.primaryButtonColor
import com.saha.androidfm.ui.theme.secondaryTextColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*@Composable
fun PrimaryButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable (RowScope.() -> Unit)
) {
    Button(
        onClick = {
            onClick.invoke()
        },
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.primaryBlue)
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        content = content
    )
}*/

/*@Composable
fun MyTextButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    text: String
){
    PrimaryButton(
        modifier = modifier,
        enabled = enabled,
        onClick = onClick
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontWeight = FontWeight(600)
            ),
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }
}*/

@Composable
fun CustomButton(
    modifier: Modifier = Modifier,
    title: String,
    @DrawableRes image: Int? = null,
    @DrawableRes leftImage: Int? = null,
    imageSize: Dp = 16.dp,
    backgroundColor: Color = primaryButtonColor,
    textColor: Color = Color.White,

    borderColor: Color? = null,
    isEnabled: Boolean = true,
    hasShadow: Boolean = false,
    imageTint: Color = Color.White,
    shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    debounceTimeMs: Long = 500, // Default 0.5 second debounce
    onClick: () -> Unit
) {
    var isClickable by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    Button(
        onClick = {
            if (isEnabled && isClickable) {
                isClickable = false
                onClick()

                // Re-enable after debounce time
                coroutineScope.launch {
                    delay(debounceTimeMs)
                    isClickable = true
                }
            }
        },
        modifier = modifier,
        enabled = isEnabled && isClickable,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        shape = shape,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (hasShadow) 4.dp else 0.dp
        ),
        border = if (borderColor != null && isEnabled && isClickable) {
            BorderStroke(1.dp, borderColor)
        } else {
            null
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 6.dp)
        ) {

            leftImage?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = "arrow",
                    modifier = Modifier.size(imageSize)
                )

                WidthGap(4.dp)
            }

            Text(
                text = title, style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isEnabled && isClickable) textColor else secondaryTextColor,
                    fontWeight = FontWeight(600)
                )
            )

            image?.let {
                WidthGap(4.dp)

                Image(
                    painter = painterResource(id = it),
                    contentDescription = "arrow",
                    modifier = Modifier.size(imageSize),
                    colorFilter = ColorFilter.tint(
                        if (isEnabled && isClickable) imageTint else secondaryTextColor
                    )
                )
            }
        }
    }
}

/*
@Composable
fun SecondaryButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable (RowScope.() -> Unit)
) {
    OutlinedButton(
        onClick = {
            onClick.invoke()
        }, modifier = modifier, enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = colorResource(R.color.primaryBlue)
        ),
        border = BorderStroke(1.dp, colorResource(R.color.primaryBlue)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        content = content
    )
}*/
