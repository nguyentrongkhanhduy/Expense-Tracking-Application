package com.example.myapplication.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    val IS_DEFAULT_CATEGORIES_INITIALIZED = booleanPreferencesKey("is_default_categories_initialized")
    val IS_GUEST_MODE = booleanPreferencesKey("is_guest_mode")
    val DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
    val LAST_SYNC_DATE = stringPreferencesKey("last_sync_date")
    val MESSAGE_PREFERENCE = stringPreferencesKey("message_preference")
}