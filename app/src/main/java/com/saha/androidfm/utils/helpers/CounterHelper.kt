package com.saha.fairdrivepartnerapp.utils.helper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CounterHelper {

    private var countdownJob: kotlinx.coroutines.Job? = null

    fun countdownTimer(
        seconds: Int,
        onStart: () -> Unit,
        onCountDown: ((remainingSeconds: Int) -> Unit)? = null,
        onCountDownFinished: () -> Unit
    ) {
        // Cancel any existing countdown
        stopCountdown()
        
        countdownJob = CoroutineScope(Dispatchers.Main).launch {
            onStart.invoke()
            var remainingSeconds = seconds

            while (remainingSeconds > 0) {
                onCountDown?.invoke(remainingSeconds)
                delay(1000)
                remainingSeconds--
            }
            onCountDownFinished()
        }
    }

    fun stopCountdown() {
        countdownJob?.cancel()
        countdownJob = null
    }
}