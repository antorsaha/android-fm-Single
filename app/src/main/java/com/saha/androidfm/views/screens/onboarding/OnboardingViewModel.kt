package com.saha.androidfm.views.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    // Inject your preferences repository here if needed
    // private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    
    fun completeOnboarding() {
        viewModelScope.launch {
            // Save that onboarding is completed
            // e.g., preferencesRepository.setOnboardingCompleted(true)
        }
    }
}
