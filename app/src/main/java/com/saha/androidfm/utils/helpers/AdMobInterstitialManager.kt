package com.saha.androidfm.utils.helpers

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Manager class for handling AdMob Interstitial Ads.
 * 
 * This class provides functionality to:
 * - Load interstitial ads in advance
 * - Show ads when requested
 * - Execute callbacks after ad is closed/dismissed
 * 
 * Usage:
 * ```
 * val adManager = AdMobInterstitialManager(context)
 * adManager.loadAd()
 * 
 * // Later, when you want to show the ad:
 * adManager.showAd(activity) { 
 *     // This callback executes after ad is closed
 *     // Play audio/video here
 * }
 * ```
 */
class AdMobInterstitialManager(private val context: android.content.Context) {
    
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    
    companion object {
        private const val TAG = "AdMobInterstitialManager"
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
        
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(
            context,
            AppConstants.INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully")
                    interstitialAd = ad
                    isLoading = false
                    
                    // Set up callbacks for ad lifecycle
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Interstitial ad dismissed")
                            // Clear the ad reference so a new one can be loaded
                            interstitialAd = null
                            // Load next ad for future use
                            loadAd()
                        }
                        
                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            Log.e(TAG, "Interstitial ad failed to show: ${error.message}")
                            interstitialAd = null
                            // Load next ad for future use
                            loadAd()
                        }
                        
                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Interstitial ad showed")
                            // Ad is showing, clear reference
                            interstitialAd = null
                        }
                    }
                }
                
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Interstitial ad failed to load: ${error.message}")
                    isLoading = false
                    interstitialAd = null
                }
            }
        )
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
        return if (interstitialAd != null) {
            // Store callback to execute after ad is closed
            val currentAd = interstitialAd
            
            // Set up callback for when ad is dismissed
            currentAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed - executing callback")
                    interstitialAd = null
                    // Execute the callback (play audio/video)
                    onAdClosed()
                    // Load next ad for future use
                    loadAd()
                }
                
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "Interstitial ad failed to show: ${error.message}")
                    interstitialAd = null
                    // If ad fails to show, still execute callback
                    onAdClosed()
                    // Load next ad for future use
                    loadAd()
                }
                
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad showed")
                    interstitialAd = null
                }
            }
            
            // Show the ad
            currentAd?.show(activity)
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
     * Checks if an ad is currently loaded and ready to show.
     */
    fun isAdLoaded(): Boolean {
        return interstitialAd != null
    }
}
