package com.nightsound.data.repository

import com.nightsound.data.local.database.NightSoundDatabase
import com.nightsound.data.local.entities.AudioSnippet
import com.nightsound.data.local.entities.RecordingSession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRepository @Inject constructor(
    private val database: NightSoundDatabase
) {

    // Audio Snippets
    fun getAllSnippets(): Flow<List<AudioSnippet>> {
        return database.audioSnippetDao().getAllSnippets()
    }

    fun getSnippetsBySession(sessionId: Long): Flow<List<AudioSnippet>> {
        return database.audioSnippetDao().getSnippetsBySession(sessionId)
    }

    suspend fun getSnippetById(id: Long): AudioSnippet? {
        return database.audioSnippetDao().getSnippetById(id)
    }

    suspend fun insertSnippet(snippet: AudioSnippet): Long {
        return database.audioSnippetDao().insert(snippet)
    }

    suspend fun updateSnippet(snippet: AudioSnippet) {
        database.audioSnippetDao().update(snippet)
    }

    suspend fun deleteSnippet(snippet: AudioSnippet) {
        database.audioSnippetDao().delete(snippet)
    }

    // Recording Sessions
    fun getAllSessions(): Flow<List<RecordingSession>> {
        return database.recordingSessionDao().getAllSessions()
    }

    suspend fun getSessionById(id: Long): RecordingSession? {
        return database.recordingSessionDao().getSessionById(id)
    }

    suspend fun getActiveSession(): RecordingSession? {
        return database.recordingSessionDao().getActiveSession()
    }

    suspend fun insertSession(session: RecordingSession): Long {
        return database.recordingSessionDao().insert(session)
    }

    suspend fun updateSession(session: RecordingSession) {
        database.recordingSessionDao().update(session)
    }

    suspend fun clearAll() {
        database.audioSnippetDao().deleteAll()
        database.recordingSessionDao().deleteAll()
    }
}
