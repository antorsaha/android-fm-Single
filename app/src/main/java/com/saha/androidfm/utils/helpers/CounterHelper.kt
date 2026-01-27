package com.saha.androidfm.utils.helpers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Helper class for managing countdown timers using coroutines.
 * 
 * This class provides functionality to start, manage, and stop countdown timers
 * with callbacks for start, countdown progress, and completion events.
 * 
 * Usage example:
 * ```
 * val counterHelper = CounterHelper()
 * counterHelper.countdownTimer(
 *     seconds = 10,
 *     onStart = { println("Countdown started") },
 *     onCountDown = { remaining -> println("$remaining seconds left") },
 *     onCountDownFinished = { println("Countdown finished") }
 * )
 * ```
 */
class CounterHelper {

    // Job reference for the active countdown timer
    private var countdownJob: kotlinx.coroutines.Job? = null

    /**
     * Starts a countdown timer that counts down from the specified number of seconds.
     * 
     * If a countdown is already running, it will be cancelled before starting the new one.
     * The countdown runs on the Main dispatcher to allow UI updates.
     * 
     * @param seconds The number of seconds to count down from
     * @param onStart Callback invoked immediately when the countdown starts
     * @param onCountDown Optional callback invoked each second with the remaining seconds
     * @param onCountDownFinished Callback invoked when the countdown reaches zero
     */
    fun countdownTimer(
        seconds: Int,
        onStart: () -> Unit,
        onCountDown: ((remainingSeconds: Int) -> Unit)? = null,
        onCountDownFinished: () -> Unit
    ) {
        // Cancel any existing countdown before starting a new one
        stopCountdown()
        
        // Launch countdown on Main dispatcher for UI thread access
        countdownJob = CoroutineScope(Dispatchers.Main).launch {
            onStart.invoke()
            var remainingSeconds = seconds

            // Count down from the specified seconds to zero
            while (remainingSeconds > 0) {
                onCountDown?.invoke(remainingSeconds)
                delay(1000) // Wait 1 second between each countdown step
                remainingSeconds--
            }
            // Invoke completion callback when countdown finishes
            onCountDownFinished()
        }
    }

    /**
     * Stops the currently running countdown timer if one exists.
     * 
     * This method cancels the countdown job and clears the reference,
     * allowing a new countdown to be started.
     */
    fun stopCountdown() {
        countdownJob?.cancel()
        countdownJob = null
    }
}