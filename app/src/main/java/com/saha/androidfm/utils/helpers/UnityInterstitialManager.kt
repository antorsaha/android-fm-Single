package com.saha.androidfm.utils.helpers

import android.app.Activity
import android.util.Log
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions

/**
 * Manager class for handling Unity Ads Interstitial Ads.
 * 
 * This class provides functionality to:
 * - Load interstitial ads in advance
 * - Show ads when requested
 * - Execute callbacks after ad is closed/dismissed
 * 
 * Usage:
 * ```
 * val adManager = UnityInterstitialManager(context)
 * adManager.loadAd()
 * 
 * // Later, when you want to show the ad:
 * adManager.showAd(activity) { 
 *     // This callback executes after ad is closed
 *     // Play audio/video here
 * }
 * ```
 */
class UnityInterstitialManager(private val context: android.content.Context) {
    
    private var isAdLoaded = false
    private var isLoading = false
    private var pendingCallback: (() -> Unit)? = null
    
    companion object {
        private const val TAG = "UnityInterstitialManager"
    }
    
    init {
        // Initialize Unity Ads if not already initialized
        if (!UnityAds.isInitialized) {
            UnityAds.initialize(
                context,
                AppConstants.UNITY_GAME_ID,
                AppConstants.UNITY_TEST_MODE,
                object : IUnityAdsInitializationListener {
                    override fun onInitializationComplete() {
                        Log.d(TAG, "Unity Ads initialized successfully")
                    }
                    
                    override fun onInitializationFailed(
                        error: UnityAds.UnityAdsInitializationError,
                        message: String
                    ) {
                        Log.e(TAG, "Unity Ads initialization failed: $message")
                    }
                }
            )
        }
    }
    
    /**
     * Loads an interstitial ad in the background.
     * 
     * This should be called early (e.g., when screen is created) to preload the ad
     * so it's ready when the user clicks the play button.
     */
    fun loadAd() {
        // Don't load if already loading or if ad is already loaded
        if (isLoading || isAdLoaded) {
            return
        }
        
        // Check if Unity Ads is initialized
        if (!UnityAds.isInitialized) {
            Log.w(TAG, "Unity Ads not initialized yet, will retry after initialization")
            return
        }
        
        isLoading = true
        
        val placementId = AppConstants.getInterstitialAdUnitId()
        
        UnityAds.load(placementId, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String) {
                Log.d(TAG, "Unity interstitial ad loaded: $placementId")
                isAdLoaded = true
                isLoading = false
            }
            
            override fun onUnityAdsFailedToLoad(
                placementId: String,
                error: UnityAds.UnityAdsLoadError,
                message: String
            ) {
                Log.e(TAG, "Unity interstitial ad failed to load: $placementId - $message (Error: $error)")
                isAdLoaded = false
                isLoading = false
            }
        })
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
        val placementId = AppConstants.getInterstitialAdUnitId()
        
        // Check if ad is loaded and Unity Ads is initialized
        if (isAdLoaded && UnityAds.isInitialized) {
            // Store callback to execute after ad is closed
            pendingCallback = onAdClosed
            
            // Show the ad
            val showOptions = UnityAdsShowOptions()
            UnityAds.show(activity, placementId, showOptions, object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(
                    placementId: String,
                    error: UnityAds.UnityAdsShowError,
                    message: String
                ) {
                    Log.e(TAG, "Unity interstitial ad failed to show: $placementId - $message (Error: $error)")
                    isAdLoaded = false
                    // Execute callback even if ad failed to show
                    pendingCallback?.invoke()
                    pendingCallback = null
                    // Try to load ad for next time
                    loadAd()
                }
                
                override fun onUnityAdsShowStart(placementId: String) {
                    Log.d(TAG, "Unity interstitial ad started: $placementId")
                }
                
                override fun onUnityAdsShowClick(placementId: String) {
                    Log.d(TAG, "Unity interstitial ad clicked: $placementId")
                }
                
                override fun onUnityAdsShowComplete(
                    placementId: String,
                    finishState: UnityAds.UnityAdsShowCompletionState
                ) {
                    Log.d(TAG, "Unity interstitial ad completed: $placementId - $finishState")
                    isAdLoaded = false
                    // Execute callback after ad is closed
                    pendingCallback?.invoke()
                    pendingCallback = null
                    // Load next ad for future use
                    loadAd()
                }
            })
            return true
        } else {
            // No ad available, execute callback immediately
            Log.d(TAG, "No Unity interstitial ad available (placement: $placementId) - executing callback immediately")
            onAdClosed()
            // Try to load ad for next time
            loadAd()
            return false
        }
    }
    
    /**
     * Cleanup method to release ad resources.
     * Call this when the manager is no longer needed.
     */
    fun destroy() {
        // Unity Ads doesn't require explicit cleanup
        // The SDK manages resources automatically
        pendingCallback = null
        isAdLoaded = false
        isLoading = false
    }
}
