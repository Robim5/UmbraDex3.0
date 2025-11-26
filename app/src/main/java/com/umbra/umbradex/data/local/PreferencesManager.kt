package com.umbra.umbradex.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "umbradex_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val BACKGROUND_MUSIC_ENABLED = booleanPreferencesKey("background_music_enabled")
        private val SOUND_EFFECTS_ENABLED = booleanPreferencesKey("sound_effects_enabled")
        private val CURRENT_THEME = stringPreferencesKey("current_theme")
    }

    // Save user session
    suspend fun saveUserSession(userId: String, email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
            preferences[USER_EMAIL] = email
            preferences[IS_LOGGED_IN] = true
        }
    }

    // Get user ID
    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID]
    }

    // Get user email
    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL]
    }

    // Check if logged in
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    // Clear session (logout)
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Background music setting
    suspend fun setBackgroundMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BACKGROUND_MUSIC_ENABLED] = enabled
        }
    }

    val backgroundMusicEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[BACKGROUND_MUSIC_ENABLED] ?: true
    }

    // Sound effects setting
    suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SOUND_EFFECTS_ENABLED] = enabled
        }
    }

    val soundEffectsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SOUND_EFFECTS_ENABLED] ?: true
    }

    // Current theme
    suspend fun setCurrentTheme(themeName: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_THEME] = themeName
        }
    }

    val currentTheme: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENT_THEME] ?: "default"
    }
}