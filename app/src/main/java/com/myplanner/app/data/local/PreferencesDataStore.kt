package com.myplanner.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Lightweight preferences via DataStore.
 * Reserved for settings and first-run flags — not user-created content.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "myplanner_preferences"
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val NOTIFICATIONS_PROMPTED = booleanPreferencesKey("notifications_prompted")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val ENTERED_VIA_EXPLORE = booleanPreferencesKey("entered_via_explore")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE] ?: "system"
    }

    val onboardingComplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETE] ?: false
    }

    val notificationsPrompted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.NOTIFICATIONS_PROMPTED] ?: false
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.NOTIFICATIONS_ENABLED] ?: false
    }

    val enteredViaExplore: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.ENTERED_VIA_EXPLORE] ?: false
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode
        }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETE] = complete
        }
    }

    suspend fun setNotificationsPrompted(prompted: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS_PROMPTED] = prompted
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setEnteredViaExplore(explore: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ENTERED_VIA_EXPLORE] = explore
        }
    }

    suspend fun completeOnboardingAfterSetup(notificationsGranted: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETE] = true
            prefs[Keys.NOTIFICATIONS_PROMPTED] = true
            prefs[Keys.NOTIFICATIONS_ENABLED] = notificationsGranted
            prefs[Keys.ENTERED_VIA_EXPLORE] = false
        }
    }

    suspend fun completeOnboardingViaExplore() {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETE] = true
            prefs[Keys.ENTERED_VIA_EXPLORE] = true
        }
    }
}
