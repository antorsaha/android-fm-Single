package com.saha.androidfm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.saha.androidfm.ui.theme.AndroidFmTheme
import com.saha.androidfm.views.App
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemUiController = rememberSystemUiController()

            // Change Status Bar color
            SideEffect {
                systemUiController.setStatusBarColor(
                    color = Color.Transparent,
                    darkIcons = false // false means white icons
                )
            }

            // Change Navigation Bar color
            SideEffect {
                systemUiController.setNavigationBarColor(
                    color = Color.Black,
                    darkIcons = false // false means white icons
                )
            }



            AndroidFmTheme {
                App()
            }
        }
    }
}