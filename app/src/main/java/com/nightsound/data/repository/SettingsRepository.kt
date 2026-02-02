package com.nightsound.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
        val NIGHT_START_HOUR = intPreferencesKey("night_start_hour")
        val NIGHT_START_MINUTE = intPreferencesKey("night_start_minute")
        val NIGHT_END_HOUR = intPreferencesKey("night_end_hour")
        val NIGHT_END_MINUTE = intPreferencesKey("night_end_minute")
        val S3_BUCKET_NAME = stringPreferencesKey("s3_bucket_name")
        val S3_REGION = stringPreferencesKey("s3_region")
        val AWS_ACCESS_KEY = stringPreferencesKey("aws_access_key")
        val AWS_SECRET_KEY = stringPreferencesKey("aws_secret_key")
    }

    // Night time settings
    val nightStartHour: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NIGHT_START_HOUR] ?: 22 // Default 10 PM
    }

    val nightStartMinute: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NIGHT_START_MINUTE] ?: 0
    }

    val nightEndHour: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NIGHT_END_HOUR] ?: 7 // Default 7 AM
    }

    val nightEndMinute: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NIGHT_END_MINUTE] ?: 0
    }

    // S3 settings
    val s3BucketName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.S3_BUCKET_NAME] ?: ""
    }

    val s3Region: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.S3_REGION] ?: "us-east-1"
    }

    val awsAccessKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AWS_ACCESS_KEY] ?: ""
    }

    val awsSecretKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AWS_SECRET_KEY] ?: ""
    }

    // Update methods
    suspend fun setNightStartTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NIGHT_START_HOUR] = hour
            preferences[PreferencesKeys.NIGHT_START_MINUTE] = minute
        }
    }

    suspend fun setNightEndTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NIGHT_END_HOUR] = hour
            preferences[PreferencesKeys.NIGHT_END_MINUTE] = minute
        }
    }

    suspend fun setS3BucketName(bucketName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.S3_BUCKET_NAME] = bucketName
        }
    }

    suspend fun setS3Region(region: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.S3_REGION] = region
        }
    }

    suspend fun setAwsCredentials(accessKey: String, secretKey: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AWS_ACCESS_KEY] = accessKey
            preferences[PreferencesKeys.AWS_SECRET_KEY] = secretKey
        }
    }
}
