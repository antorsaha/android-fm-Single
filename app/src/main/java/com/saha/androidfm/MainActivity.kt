package com.saha.androidfm

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.saha.androidfm.ui.theme.AndroidFmTheme
import com.saha.androidfm.utils.helpers.AppHelper
import com.saha.androidfm.views.App
import dagger.hilt.android.AndroidEntryPoint


/**
 * Main activity of the application.
 * 
 * This activity serves as the entry point for the app and handles:
 * - Edge-to-edge display setup
 * - Notification permission requests (Android 13+)
 * - Firebase Analytics initialization
 * - System UI configuration (status bar, navigation bar)
 * - Compose content setup
 * 
 * The activity uses Hilt for dependency injection and Compose for UI.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    /**
     * Activity result launcher for notification permission requests.
     * 
     * This launcher handles the result of the notification permission request
     * for Android 13+ (API 33+). If permission is denied, the app will still
     * function but notifications may not display properly.
     */
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, notifications will work properly
        } else {
            // Permission denied, user will need to grant it manually from settings
            // The app will continue to function, but notifications may not display
        }
    }

    // Firebase Analytics instance for tracking app events
    private lateinit var analytics: FirebaseAnalytics
    
    /**
     * Called when the activity is first created.
     * 
     * Sets up the activity's UI, permissions, and system configurations.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display for modern Android look
        enableEdgeToEdge()
        
        // Request notification permission for Android 13+ (API 33+)
        // This permission is required for foreground service notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!AppHelper.hasNotificationPermission(this)) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Initialize Firebase Analytics for event tracking
        analytics = Firebase.analytics
        
        // Set up Compose content
        setContent {
            val systemUiController = rememberSystemUiController()

            // Configure status bar to be transparent with white icons
            // This allows content to extend behind the status bar (edge-to-edge)
            SideEffect {
                systemUiController.setStatusBarColor(
                    color = Color.Transparent,
                    darkIcons = false // false means white icons (for dark backgrounds)
                )
            }

            // Configure navigation bar to be black with white icons
            // This provides a consistent dark theme for the navigation bar
            SideEffect {
                systemUiController.setNavigationBarColor(
                    color = Color.Black,
                    darkIcons = false // false means white icons
                )
            }

            // Apply app theme and display main app content
            AndroidFmTheme {
                App()
            }
        }
    }
}