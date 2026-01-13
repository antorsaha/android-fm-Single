package com.saha.androidfm.views.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.LottieConstants
import com.saha.androidfm.R
import com.saha.androidfm.views.components.LottieAnimationView

@Composable
fun AppLoader(isCancelable: Boolean = false) {

    Dialog(
        onDismissRequest = { }, properties = DialogProperties(
            dismissOnBackPress = isCancelable, dismissOnClickOutside = isCancelable
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

            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                LottieAnimationView(
                    animationResId = R.raw.anim_loading,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier
                        .height(75.dp)
                        .width(75.dp)
                )
            }
        }
    }
}