package com.saha.androidfm.views.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun CircularAnimatedImage(
    painter: Painter,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    imageSize: Dp = 64.dp,
    animationDuration: Int = 3000, // Slow animation (3 seconds per circle)
    repetitions: Int = 2,
    contentDescription: String? = null
) {
    var currentRotation by remember { mutableStateOf(0f) }
    var animationStartTime by remember { mutableStateOf(0L) }
    var hasCompletedRotations by remember { mutableStateOf(false) }
    var wasPlaying by remember { mutableStateOf(false) }
    
    // Reset when playing state changes
    LaunchedEffect(isPlaying) {
        if (isPlaying && !wasPlaying) {
            // Start new animation cycle - always reset rotation when starting
            currentRotation = 0f
            animationStartTime = System.currentTimeMillis()
            hasCompletedRotations = false
        } else if (!isPlaying && wasPlaying) {
            // When pausing, reset to 0 degrees (show original image)
            currentRotation = 0f
            hasCompletedRotations = true
        }
        wasPlaying = isPlaying
    }
    
    // Animate the rotation smoothly
    val animatedRotation by animateFloatAsState(
        targetValue = currentRotation,
        animationSpec = if (isPlaying && !hasCompletedRotations) {
            tween(
                durationMillis = 16, // Smooth 60fps animation
                easing = LinearEasing
            )
        } else {
            // When paused, animate back to 0 degrees (original image)
            tween(
                durationMillis = 300, // Smooth transition back to 0
                easing = LinearEasing
            )
        },
        label = "rotation_animation"
    )
    
    // Update rotation continuously for spinning motion when playing
    LaunchedEffect(isPlaying, hasCompletedRotations) {
        if (isPlaying && !hasCompletedRotations) {
            val totalDuration = animationDuration * repetitions
            val totalDegrees = 360f * repetitions // 2 full rotations = 720 degrees
            
            while (isPlaying && !hasCompletedRotations) {
                val elapsed = System.currentTimeMillis() - animationStartTime
                val progress = (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
                
                currentRotation = totalDegrees * progress
                
                // Check if we've completed all rotations
                if (progress >= 1f) {
                    hasCompletedRotations = true
                    currentRotation = totalDegrees // Final position (720 degrees for 2 rotations)
                    break
                }
                
                // Stop if audio stopped - will reset to 0 in LaunchedEffect
                if (!isPlaying) {
                    break
                }
                
                delay(16) // ~60fps
            }
        }
    }
    
    Box(
        modifier = modifier
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier
                .size(imageSize)
                .rotate(animatedRotation) // Rotate in place
        )
    }
}
