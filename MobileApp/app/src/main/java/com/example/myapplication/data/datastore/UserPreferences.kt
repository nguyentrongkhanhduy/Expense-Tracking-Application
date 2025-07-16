package com.example.myapplication.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_preferences")

object UserPreferences {
    // First-time launch
    suspend fun isFirstLaunch(context: Context): Boolean {
        return context.dataStore.data
            .map { it[PreferencesKeys.IS_FIRST_LAUNCH] ?: true }
            .first()
    }

    suspend fun setFirstLaunch(context: Context, isFirst: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.IS_FIRST_LAUNCH] = isFirst
        }
    }

    // Guest mode
    suspend fun isGuestMode(context: Context): Boolean {
        return context.dataStore.data
            .map { it[PreferencesKeys.IS_GUEST_MODE] ?: false }
            .first()
    }

    suspend fun setGuestMode(context: Context, isGuest: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.IS_GUEST_MODE] = isGuest
        }
    }

    suspend fun getCurrency(context: Context): String {
        return context.dataStore.data
            .map { it[PreferencesKeys.DEFAULT_CURRENCY] ?: "Canadian Dollar" }
            .first()
    }

    suspend fun setCurrency(context: Context, currency: String) {
        context.dataStore.edit {
            it[PreferencesKeys.DEFAULT_CURRENCY] = currency
        }
    }

    // Initialize category
    suspend fun isInitialized(context: Context): Boolean {
        return context.dataStore.data
            .map { it[PreferencesKeys.IS_DEFAULT_CATEGORIES_INITIALIZED] ?: false }
            .first()
    }

    suspend fun setInitialized(context: Context) {
        context.dataStore.edit {
            it[PreferencesKeys.IS_DEFAULT_CATEGORIES_INITIALIZED] = true
        }
    }

    suspend fun getLastSyncDate(context: Context): String {
        return context.dataStore.data
            .map { it[PreferencesKeys.LAST_SYNC_DATE] ?: "" }
            .first()
    }

    suspend fun setLastSyncDate(context: Context, date: String) {
        context.dataStore.edit {
            it[PreferencesKeys.LAST_SYNC_DATE] = date
        }
    }

    suspend fun getMessagePreference(context: Context): String {
        return context.dataStore.data
            .map { it[PreferencesKeys.MESSAGE_PREFERENCE] ?: "Off" }
            .first()
    }

    suspend fun setMessagePreference(context: Context, preference: String) {
        context.dataStore.edit {
            it[PreferencesKeys.MESSAGE_PREFERENCE] = preference
        }
    }
}