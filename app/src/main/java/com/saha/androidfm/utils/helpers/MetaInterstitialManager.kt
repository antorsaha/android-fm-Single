package com.saha.androidfm.utils.helpers

import android.app.Activity
import android.util.Log
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener

/**
 * Manager class for handling Meta (Facebook) Interstitial Ads.
 * 
 * This class provides functionality to:
 * - Load interstitial ads in advance
 * - Show ads when requested
 * - Execute callbacks after ad is closed/dismissed
 * 
 * Usage:
 * ```
 * val adManager = MetaInterstitialManager(context)
 * adManager.loadAd()
 * 
 * // Later, when you want to show the ad:
 * adManager.showAd(activity) { 
 *     // This callback executes after ad is closed
 *     // Play audio/video here
 * }
 * ```
 */
class MetaInterstitialManager(private val context: android.content.Context) {
    
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var pendingCallback: (() -> Unit)? = null
    
    companion object {
        private const val TAG = "MetaInterstitialManager"
    }
    
    /**
     * Loads an interstitial ad in the background.
     * 
     * This should be called early (e.g., when screen is created) to preload the ad
     * so it's ready when the user clicks the play button.
     */
    fun loadAd() {
        // Don't load if already loading or if ad is already loaded
        if (isLoading || interstitialAd != null) {
            return
        }
        
        isLoading = true
        
        val placementId = AppConstants.getInterstitialAdUnitId()
        
        // Create InterstitialAd
        interstitialAd = InterstitialAd(context, placementId)
        
        // Create listener
        val listener = object : InterstitialAdListener {
            override fun onInterstitialDisplayed(ad: Ad) {
                Log.d(TAG, "Interstitial ad displayed")
            }
            
            override fun onInterstitialDismissed(ad: Ad) {
                Log.d(TAG, "Interstitial ad dismissed")
                interstitialAd = null
                // Execute pending callback if exists
                pendingCallback?.invoke()
                pendingCallback = null
                // Load next ad for future use
                loadAd()
            }
            
            override fun onError(ad: Ad, error: AdError) {
                Log.e(TAG, "Interstitial ad error: ${error.errorMessage} (Code: ${error.errorCode})")
                isLoading = false
                interstitialAd = null
                // If there's a pending callback and ad failed, execute it anyway
                pendingCallback?.invoke()
                pendingCallback = null
            }
            
            override fun onAdLoaded(ad: Ad) {
                Log.d(TAG, "Interstitial ad loaded successfully")
                isLoading = false
            }
            
            override fun onAdClicked(ad: Ad) {
                Log.d(TAG, "Interstitial ad clicked")
            }
            
            override fun onLoggingImpression(ad: Ad) {
                Log.d(TAG, "Interstitial ad impression logged")
            }
        }
        
        // Load ad with listener using buildLoadAdConfig
        try {
            interstitialAd?.buildLoadAdConfig()
                ?.withAdListener(listener)
                ?.build()
                ?.let { config ->
                    interstitialAd?.loadAd(config)
                }
        } catch (e: Exception) {
            // If buildLoadAdConfig is not available, try alternative approach
            Log.w(TAG, "buildLoadAdConfig not available, trying alternative: ${e.message}")
            try {
                // Try reflection to set listener if method exists
                val setListenerMethod = interstitialAd?.javaClass?.methods?.find { 
                    it.name == "setAdListener" || it.name == "setInterstitialAdListener" 
                }
                setListenerMethod?.invoke(interstitialAd, listener)
                interstitialAd?.loadAd()
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to set listener: ${e2.message}")
                // Load without listener as last resort
                interstitialAd?.loadAd()
            }
        }
    }
    
    /**
     * Shows the interstitial ad if available, otherwise executes the callback immediately.
     * 
     * @param activity The Activity context required to show the ad
     * @param onAdClosed Callback function that executes after the ad is closed/dismissed.
     *                   This is where you should play audio/video.
     * 
     * @return true if ad was shown, false if ad was not available (callback still executes)
     */
    fun showAd(activity: Activity, onAdClosed: () -> Unit): Boolean {
        return if (interstitialAd != null && interstitialAd!!.isAdLoaded) {
            // Store callback to execute after ad is closed
            pendingCallback = onAdClosed
            
            // Show the ad
            interstitialAd?.show()
            true
        } else {
            // No ad available, execute callback immediately
            Log.d(TAG, "No interstitial ad available - executing callback immediately")
            onAdClosed()
            // Try to load ad for next time
            loadAd()
            false
        }
    }
    
    /**
     * Cleanup method to release ad resources.
     * Call this when the manager is no longer needed.
     */
    fun destroy() {
        interstitialAd?.destroy()
        interstitialAd = null
        pendingCallback = null
    }
}
