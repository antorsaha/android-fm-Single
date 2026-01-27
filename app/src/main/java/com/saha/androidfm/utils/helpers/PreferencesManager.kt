package com.saha.androidfm.utils.helpers

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

/**
 * Singleton class for managing app preferences using SharedPreferences.
 * 
 * This class provides a clean interface for storing and retrieving app settings,
 * such as onboarding completion status. It supports both Hilt dependency injection
 * and direct instantiation for use in composables.
 * 
 * Usage with Hilt:
 * ```
 * @Inject lateinit var preferencesManager: PreferencesManager
 * ```
 * 
 * Usage in Composables:
 * ```
 * val preferencesManager = remember { PreferencesManager.create(context) }
 * ```
 */
@Singleton
class PreferencesManager private constructor(
    private val sharedPreferences: SharedPreferences
) {
    /**
     * Hilt-injectable constructor for dependency injection.
     * 
     * @param context Application context (provided by Hilt)
     */
    @Inject
    constructor(@ApplicationContext context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    /**
     * Companion object containing preference keys and factory method.
     */
    companion object {
        /** SharedPreferences file name */
        private const val PREFS_NAME = "fm_radio_prefs"
        
        /** Key for storing onboarding completion status */
        private const val KEY_ONBOARDING_COMPLETED = "is_onboarding_completed"

        /**
         * Factory function for direct instantiation (for use in composables without DI).
         * 
         * @param context Context to access SharedPreferences
         * @return PreferencesManager instance
         */
        fun create(context: Context): PreferencesManager {
            return PreferencesManager(
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            )
        }
    }

    /**
     * Saves the onboarding completion status.
     * 
     * @param completed True if onboarding has been completed, false otherwise
     */
    fun setOnboardingCompleted(completed: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_ONBOARDING_COMPLETED, completed) }
    }

    /**
     * Checks if onboarding has been completed.
     * 
     * @return True if onboarding is completed, false otherwise (defaults to false)
     */
    fun isOnboardingCompleted(): Boolean {
        return sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }
}
