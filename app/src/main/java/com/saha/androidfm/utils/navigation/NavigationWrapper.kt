package com.saha.androidfm.utils.navigation

import kotlinx.serialization.Serializable

@Serializable
data class NavigationWrapper(
    val data: String? = null,
    val screenName: String
)