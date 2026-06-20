package com.example.testing.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

object ThemePreference {
    private val THEME_KEY = booleanPreferencesKey("dark_theme")
    private val FIRST_LAUNCH_KEY = booleanPreferencesKey("first_launch")
    private val BALANCE_VISIBILITY_KEY = booleanPreferencesKey("balance_visibility")

    suspend fun saveTheme(context: Context, isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = isDark
        }
    }

    fun getTheme(context: Context): Flow<Boolean?> {
        return context.dataStore.data.map { preferences ->
            preferences[THEME_KEY]
        }
    }

    suspend fun setFirstLaunchDone(context: Context) {
        context.dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH_KEY] = false
        }
    }

    fun isFirstLaunch(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[FIRST_LAUNCH_KEY] ?: true
        }
    }

    suspend fun saveBalanceVisibility(context: Context, isVisible: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BALANCE_VISIBILITY_KEY] = isVisible
        }
    }

    fun getBalanceVisibility(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[BALANCE_VISIBILITY_KEY] ?: true
        }
    }
}
