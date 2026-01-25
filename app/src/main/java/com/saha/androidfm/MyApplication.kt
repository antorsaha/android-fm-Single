package com.saha.androidfm

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.saha.androidfm.utils.helpers.AdNetwork
import com.saha.androidfm.utils.helpers.AppConstants
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize ad network SDK based on configuration
        when (AppConstants.AD_NETWORK) {
            AdNetwork.META -> {
                // Meta SDK is initialized automatically when first ad is loaded
                // No explicit initialization needed
            }
            AdNetwork.UNITY -> {
                // Unity Ads SDK is initialized in UnityInterstitialManager
                // No explicit initialization needed here
            }
            AdNetwork.ADMOB -> {
                // Initialize AdMob SDK
                MobileAds.initialize(this) { initializationStatus ->
                    // Initialization complete
                }
            }
        }
    }
}