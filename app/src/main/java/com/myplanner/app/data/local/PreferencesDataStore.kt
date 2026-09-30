package com.myplanner.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

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
        val PLAN_FILTER = stringPreferencesKey("plan_filter")
        val IDEA_CATEGORY_FILTER = stringPreferencesKey("idea_category_filter")
        val REMINDER_NOTIFICATIONS = booleanPreferencesKey("reminder_notifications")
        val VIBRATE_ON_REMINDER = booleanPreferencesKey("vibrate_on_reminder")
        val NOTIFY_MISSED = booleanPreferencesKey("notify_missed_reminders")
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
    val reminderNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.REMINDER_NOTIFICATIONS] ?: true
    }
    val vibrateOnReminder: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.VIBRATE_ON_REMINDER] ?: true
    }
    val notifyMissedReminders: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.NOTIFY_MISSED] ?: true
    }
    val planFilter: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.PLAN_FILTER] ?: "ALL"
    }
    val ideaCategoryFilter: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.IDEA_CATEGORY_FILTER] ?: "all"
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs -> prefs[Keys.THEME_MODE] = mode }
    }
    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.ONBOARDING_COMPLETE] = complete }
    }
    suspend fun setNotificationsPrompted(prompted: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.NOTIFICATIONS_PROMPTED] = prompted }
    }
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }
    suspend fun setEnteredViaExplore(explore: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.ENTERED_VIA_EXPLORE] = explore }
    }
    suspend fun setReminderNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.REMINDER_NOTIFICATIONS] = enabled }
    }
    suspend fun setVibrateOnReminder(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.VIBRATE_ON_REMINDER] = enabled }
    }
    suspend fun setNotifyMissedReminders(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.NOTIFY_MISSED] = enabled }
    }
    suspend fun setPlanFilter(filter: String) {
        context.dataStore.edit { prefs -> prefs[Keys.PLAN_FILTER] = filter }
    }
    suspend fun setIdeaCategoryFilter(filter: String) {
        context.dataStore.edit { prefs -> prefs[Keys.IDEA_CATEGORY_FILTER] = filter }
    }

    suspend fun snapshot(): Map<String, String> {
        val prefs = context.dataStore.data.first()
        return buildMap {
            put("theme_mode", prefs[Keys.THEME_MODE] ?: "system")
            put("reminder_notifications", (prefs[Keys.REMINDER_NOTIFICATIONS] ?: true).toString())
            put("vibrate_on_reminder", (prefs[Keys.VIBRATE_ON_REMINDER] ?: true).toString())
            put("notify_missed_reminders", (prefs[Keys.NOTIFY_MISSED] ?: true).toString())
        }
    }

    suspend fun applySnapshot(map: Map<String, String>) {
        context.dataStore.edit { prefs ->
            map["theme_mode"]?.let { prefs[Keys.THEME_MODE] = it }
            map["reminder_notifications"]?.let { prefs[Keys.REMINDER_NOTIFICATIONS] = it.toBoolean() }
            map["vibrate_on_reminder"]?.let { prefs[Keys.VIBRATE_ON_REMINDER] = it.toBoolean() }
            map["notify_missed_reminders"]?.let { prefs[Keys.NOTIFY_MISSED] = it.toBoolean() }
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
