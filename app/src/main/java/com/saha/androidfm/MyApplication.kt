package com.saha.androidfm

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.saha.androidfm.utils.helpers.AdNetwork
import com.saha.androidfm.utils.helpers.AppConstants
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for the Android FM Radio app.
 * 
 * This class handles application-level initialization, including:
 * - Hilt dependency injection setup
 * - Ad network SDK initialization (AdMob, Meta, or Unity)
 * 
 * The app uses Hilt for dependency injection, which requires this class
 * to be annotated with @HiltAndroidApp.
 */
@HiltAndroidApp
class MyApplication: Application() {
    /**
     * Called when the application is starting, before any activity is created.
     * 
     * This is where we initialize SDKs and other application-level components.
     */
    override fun onCreate() {
        super.onCreate()
        
        // Initialize the selected ad network SDK
        // The ad network is configured in AppConstants.AD_NETWORK
        when (AppConstants.AD_NETWORK) {
            AdNetwork.META -> {
                // Meta (Facebook) SDK is initialized automatically when first ad is loaded
                // No explicit initialization needed here
            }
            AdNetwork.UNITY -> {
                // Unity Ads SDK is initialized in UnityInterstitialManager
                // No explicit initialization needed here
            }
            AdNetwork.ADMOB -> {
                // Initialize AdMob SDK
                // This must be called before loading any AdMob ads
                MobileAds.initialize(this) { initializationStatus ->
                    // Initialization complete
                    // The initializationStatus can be used to check if initialization was successful
                }
            }
        }
    }
}