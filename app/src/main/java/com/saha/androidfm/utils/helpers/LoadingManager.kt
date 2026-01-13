package com.saha.androidfm.utils.helpers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object LoadingManager {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun showLoading(){
        _isLoading.value = true
    }

    fun hideLoading(){
        _isLoading.value = false
    }
}