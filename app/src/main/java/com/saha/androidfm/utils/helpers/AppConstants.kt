package com.saha.androidfm.utils.helpers

/**
 * Constants object containing app-wide configuration values.
 * 
 * This object stores:
 * - Radio station configuration (URL, name, frequency)
 * - Live stream video URL
 * - Website and contact information
 * - Social media URLs
 * - Ad network configuration and ad unit IDs
 * 
 * All values should be updated before building for production.
 */
object AppConstants {

    // ==================== Radio Station Configuration ====================
    /** Radio station stream URL (supports HTTP, HTTPS, M3U, M3U8 formats) */
    const val STATION_SEAM_URL = "http://118.179.219.244:8000/;?n=d70ecddb2aebf420177c"
    
    /** Radio station frequency (display only) */
    const val STATION_FREQUENCY = "88.9"
    
    /** Radio station name displayed in UI and notifications */
    const val STATION_NAME = "ABC FM"
    
    // ==================== Live Stream Configuration ====================
    /** Live stream video URL (HLS format - .m3u8) */
    const val LIVE_STREAM_VIDEO_URL = "https://nmxlive.akamaized.net/hls/live/529965/Live_1/index.m3u8"

    // ==================== Website and Contact Information ====================
    /** Main website URL */
    const val WEBSITE_URL = "https://www.google.com/"
    
    /** Contact email address */
    const val CONTACT_ADDRESS = "abcfm@gmail.com"
    
    /** About Us page URL */
    const val ABOUT_US_URL = "https://www.google.com/"
    
    /** Privacy Policy page URL */
    const val PRIVACY_POLICY_URL = "https://www.google.com/"
    
    /** Terms of Use page URL */
    const val TERMS_OF_USE_URL = "https://www.google.com/"

    // ==================== Social Media URLs ====================
    /** Facebook page/profile URL */
    const val FACEBOOK_URL = "https://www.facebook.com/"
    
    /** Instagram profile URL */
    const val INSTAGRAM_URL = "https://www.instagram.com/"
    
    /** TikTok profile URL */
    const val TIKTOK_URL = "https://www.tiktok.com/"
    
    // ==================== Ad Network Configuration ====================
    /**
     * Selected ad network for the app.
     * 
     * Change this to switch between ad networks:
     * - AdNetwork.ADMOB for Google AdMob
     * - AdNetwork.META for Meta (Facebook) Audience Network
     * - AdNetwork.UNITY for Unity Ads
     * 
     * Make sure to update the corresponding ad unit IDs below.
     */
    val AD_NETWORK = AdNetwork.ADMOB
    
    // AdMob Configuration
    // Replace with your actual values from AdMob console
    const val ADMOB_APPLICATION_ID = "ca-app-pub-3940256099942544~3347511713" // Test application ID
    const val ADMOB_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111" // Test banner ad unit
    const val ADMOB_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712" // Test interstitial ad unit
    
    // Meta (Facebook) Ads Configuration
    // Replace with your actual placement IDs from Meta Audience Network
    const val META_BANNER_PLACEMENT_ID = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID" // Test placement ID
    const val META_INTERSTITIAL_PLACEMENT_ID = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID" // Test placement ID
    
    // Unity Ads Configuration
    // Replace with your actual Game ID and Placement IDs from Unity Ads dashboard
    const val UNITY_GAME_ID = "YOUR_UNITY_GAME_ID" // Your Unity Ads Game ID
    const val UNITY_BANNER_PLACEMENT_ID = "Banner_Android" // Banner placement ID
    const val UNITY_INTERSTITIAL_PLACEMENT_ID = "Interstitial_Android" // Interstitial placement ID
    const val UNITY_TEST_MODE = true // Set to false for production
    
    // ==================== Ad Unit ID Helper Functions ====================
    /**
     * Returns the banner ad unit ID for the currently selected ad network.
     * 
     * @return Banner ad unit ID string
     */
    fun getBannerAdUnitId(): String {
        return when (AD_NETWORK) {
            AdNetwork.META -> META_BANNER_PLACEMENT_ID
            AdNetwork.UNITY -> UNITY_BANNER_PLACEMENT_ID
            AdNetwork.ADMOB -> ADMOB_BANNER_AD_UNIT_ID
        }
    }
    
    /**
     * Returns the interstitial ad unit ID for the currently selected ad network.
     * 
     * @return Interstitial ad unit ID string
     */
    fun getInterstitialAdUnitId(): String {
        return when (AD_NETWORK) {
            AdNetwork.META -> META_INTERSTITIAL_PLACEMENT_ID
            AdNetwork.UNITY -> UNITY_INTERSTITIAL_PLACEMENT_ID
            AdNetwork.ADMOB -> ADMOB_INTERSTITIAL_AD_UNIT_ID
        }
    }
}