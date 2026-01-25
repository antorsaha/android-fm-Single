package com.saha.androidfm.utils.helpers

import android.app.Activity
import android.content.Context

/**
 * Interface for unified ad management across different ad networks.
 * 
 * This interface allows switching between AdMob and Meta Ads
 * without changing the code in screens that use ads.
 */
interface AdManager {
    /**
     * Loads an interstitial ad in the background.
     * Should be called early (e.g., when screen is created).
     */
    fun loadAd()
    
    /**
     * Shows the interstitial ad if available, otherwise executes the callback immediately.
     * 
     * @param activity The Activity context required to show the ad
     * @param onAdClosed Callback function that executes after the ad is closed/dismissed.
     * 
     * @return true if ad was shown, false if ad was not available
     */
    fun showAd(activity: Activity, onAdClosed: () -> Unit): Boolean
    
    /**
     * Cleanup method to release ad resources.
     * Call this when the manager is no longer needed.
     */
    fun destroy()
}

/**
 * Factory function to create the appropriate AdManager based on AppConstants.AD_NETWORK.
 * 
 * @param context The application context
 * @return An AdManager instance (AdMobInterstitialManager, MetaInterstitialManager, or UnityInterstitialManager)
 */
fun createAdManager(context: Context): AdManager {
    return when (AppConstants.AD_NETWORK) {
        AdNetwork.META -> MetaInterstitialManagerWrapper(context)
        AdNetwork.UNITY -> UnityInterstitialManagerWrapper(context)
        AdNetwork.ADMOB -> AdMobInterstitialManagerWrapper(context)
    }
}

/**
 * Wrapper class to make AdMobInterstitialManager conform to AdManager interface.
 */
private class AdMobInterstitialManagerWrapper(context: Context) : AdManager {
    private val manager = AdMobInterstitialManager(context)
    
    override fun loadAd() {
        manager.loadAd()
    }
    
    override fun showAd(activity: Activity, onAdClosed: () -> Unit): Boolean {
        return manager.showAd(activity, onAdClosed)
    }
    
    override fun destroy() {
        // AdMobInterstitialManager doesn't have a destroy method, but we can clear references
        // The manager will handle cleanup automatically
    }
}

/**
 * Wrapper class to make MetaInterstitialManager conform to AdManager interface.
 */
private class MetaInterstitialManagerWrapper(context: Context) : AdManager {
    private val manager = MetaInterstitialManager(context)
    
    override fun loadAd() {
        manager.loadAd()
    }
    
    override fun showAd(activity: Activity, onAdClosed: () -> Unit): Boolean {
        return manager.showAd(activity, onAdClosed)
    }
    
    override fun destroy() {
        manager.destroy()
    }
}

/**
 * Wrapper class to make UnityInterstitialManager conform to AdManager interface.
 */
private class UnityInterstitialManagerWrapper(context: Context) : AdManager {
    private val manager = UnityInterstitialManager(context)
    
    override fun loadAd() {
        manager.loadAd()
    }
    
    override fun showAd(activity: Activity, onAdClosed: () -> Unit): Boolean {
        return manager.showAd(activity, onAdClosed)
    }
    
    override fun destroy() {
        manager.destroy()
    }
}
