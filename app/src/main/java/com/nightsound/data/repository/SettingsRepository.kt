package com.nightsound.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
}
