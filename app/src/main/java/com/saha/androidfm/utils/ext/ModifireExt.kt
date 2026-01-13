package com.saha.androidfm.utils.ext

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.dashedBorder(
    strokeWidth: Dp,
    color: Color,
    cornerRadius: Dp = 0.dp,
    intervals: FloatArray = floatArrayOf(10f, 10f) // [dashLength, gapLength]
): Modifier = this.then(
    Modifier.drawBehind {
        val stroke = Stroke(
            width = strokeWidth.toPx(), pathEffect = PathEffect.dashPathEffect(intervals)
        )

        val rect = Rect(
            left = strokeWidth.toPx() / 2,
            top = strokeWidth.toPx() / 2,
            right = size.width - strokeWidth.toPx() / 2,
            bottom = size.height - strokeWidth.toPx() / 2
        )

        drawRoundRect(
            color = color,
            topLeft = rect.topLeft,
            size = rect.size,
            style = stroke,
            cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
        )
    })


private var lastClickTime = 0L

fun Modifier.singleClick(
    enabled: Boolean = true,
    delayMillis: Long = 500L,
    onClick: () -> Unit
): Modifier {
    return clickable(enabled = enabled) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > delayMillis) {
            lastClickTime = currentTime
            onClick()
        }
    }
}