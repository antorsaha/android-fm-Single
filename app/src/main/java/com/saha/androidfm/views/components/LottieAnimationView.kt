package com.saha.androidfm.views.components

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition


@Composable
fun LottieAnimationView(modifier: Modifier = Modifier,
                        @RawRes animationResId: Int,
                        iterations: Int = LottieConstants.IterateForever) {
    val preloaderLottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            animationResId
        )
    )

    val preloaderProgress by animateLottieCompositionAsState(
        preloaderLottieComposition, iterations = iterations, isPlaying = true
    )

    LottieAnimation(
        composition = preloaderLottieComposition, progress = preloaderProgress, modifier = modifier
    )
}