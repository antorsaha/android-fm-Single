package com.saha.androidfm.ui.theme

import androidx.compose.ui.graphics.Color

// MARK: - Legacy Colors (keeping for compatibility)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// MARK: - Backgrounds
val backgroundColor = Color(0xFF1C1726)
val surface = Color(0xFF2E2938)
val cardBackgroundColor = Color(0xFF1C1C1E) // Keep for backward compatibility

// MARK: - Text Colors
val primaryTextColor = Color(0xFFFFFFFF)
val secondaryTextColor = Color(0xFF9994A1)
val tabBarColor = Color(0xFF6D6D6D) // Keep for backward compatibility

// MARK: - Accent Colors
val accentRed = Color(0xFFF5596E)
val accent = Color(0xFFA656F7) // Main accent color from Assets

// MARK: - Button Colors (keeping existing for backward compatibility)
val primaryButtonColor = Color(0xFF74A0FD)
val primaryButtonColor20 = Color(0x30D38306)
val primaryButtonColor50 = Color(0x50D38306)
val secondaryButtonColor = Color(0xFF2D2D2D)

// MARK: - Station Icon Colors
val stationIconPurple = Color(0xFFB24DE6)
val stationIconBlue = Color(0xFF3366CC)
val stationIconOrange = Color(0xFFE6664D)
val stationIconGreen = Color(0xFF4DB366)
val stationIconRed = Color(0xFFE63333)

// MARK: - Gradient Colors
val purpleGradientStart = Color(0xFFB24DE6)
val purpleGradientEnd = Color(0xFF8C38CC)
val purpleGradientAlternative = Color(0xFFCC38D1)
val onboardingPurple = Color(0xFFAB4AED)
val goldGradientStart = Color(0xFFE6B333)
val goldGradientEnd = Color(0xFFF2CC4D)

// MARK: - System/Utility Colors
val white = Color(0xFFFFFFFF)
val grayLight = Color(0xFF1A1A1A) // Gray at ~10% opacity approximation
val darkGray = Color(0xFF555555) // System darkGray approximation
val systemRed = Color(0xFFFF3B30) // iOS system red approximation

// MARK: - Opacity Variants (white with opacity on dark background)
val whiteOpacity15 = Color(0x26262626) // White at 15% opacity
val whiteOpacity30 = Color(0x4D4D4D4D) // White at 30% opacity
val whiteOpacity60 = Color(0x99999999) // White at 60% opacity

// MARK: - Color Arrays
val stationIconColors = listOf(
    stationIconPurple,
    stationIconBlue,
    stationIconOrange,
    stationIconGreen,
    stationIconRed
)