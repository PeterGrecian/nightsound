package com.nightsound.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val SNIPPET_COUNT = intPreferencesKey("snippet_count")
        val CHUNK_DURATION_SECONDS = intPreferencesKey("chunk_duration_seconds")

        // Periodic save settings
        val PERIODIC_SAVE_ENABLED = booleanPreferencesKey("periodic_save_enabled")
        val PERIODIC_SAVE_COUNT = intPreferencesKey("periodic_save_count")
        val PERIODIC_SAVE_INTERVAL_MINUTES = intPreferencesKey("periodic_save_interval_minutes")

        // Auto-stop settings
        val AUTO_STOP_ENABLED = booleanPreferencesKey("auto_stop_enabled")
        val AUTO_STOP_HOUR = intPreferencesKey("auto_stop_hour")
        val AUTO_STOP_MINUTE = intPreferencesKey("auto_stop_minute")

        // Delayed start settings
        val DELAYED_START_ENABLED = booleanPreferencesKey("delayed_start_enabled")
        val DELAYED_START_MINUTES = intPreferencesKey("delayed_start_minutes")
    }

    val snippetCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SNIPPET_COUNT] ?: 3
    }

    val chunkDurationSeconds: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CHUNK_DURATION_SECONDS] ?: 10
    }

    suspend fun setSnippetCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SNIPPET_COUNT] = count
        }
    }

    suspend fun setChunkDurationSeconds(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CHUNK_DURATION_SECONDS] = seconds
        }
    }

    // Periodic save settings
    val periodicSaveEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PERIODIC_SAVE_ENABLED] ?: false
    }

    val periodicSaveCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PERIODIC_SAVE_COUNT] ?: 2
    }

    val periodicSaveIntervalMinutes: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PERIODIC_SAVE_INTERVAL_MINUTES] ?: 60
    }

    suspend fun setPeriodicSaveEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PERIODIC_SAVE_ENABLED] = enabled
        }
    }

    suspend fun setPeriodicSaveCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PERIODIC_SAVE_COUNT] = count
        }
    }

    suspend fun setPeriodicSaveIntervalMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PERIODIC_SAVE_INTERVAL_MINUTES] = minutes
        }
    }

    // Auto-stop settings
    val autoStopEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_STOP_ENABLED] ?: false
    }

    val autoStopHour: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_STOP_HOUR] ?: 6
    }

    val autoStopMinute: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_STOP_MINUTE] ?: 0
    }

    suspend fun setAutoStopEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_STOP_ENABLED] = enabled
        }
    }

    suspend fun setAutoStopHour(hour: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_STOP_HOUR] = hour
        }
    }

    suspend fun setAutoStopMinute(minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_STOP_MINUTE] = minute
        }
    }

    // Delayed start settings
    val delayedStartEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DELAYED_START_ENABLED] ?: false
    }

    val delayedStartMinutes: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DELAYED_START_MINUTES] ?: 30
    }

    suspend fun setDelayedStartEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DELAYED_START_ENABLED] = enabled
        }
    }

    suspend fun setDelayedStartMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DELAYED_START_MINUTES] = minutes
        }
    }
}
