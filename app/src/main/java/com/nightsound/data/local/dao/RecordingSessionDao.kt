package com.nightsound.data.local.dao

import androidx.room.*
import com.nightsound.data.local.entities.RecordingSession
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingSessionDao {

    @Insert
    suspend fun insert(session: RecordingSession): Long

    @Update
    suspend fun update(session: RecordingSession)

    @Delete
    suspend fun delete(session: RecordingSession)

    @Query("SELECT * FROM recording_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<RecordingSession>>

    @Query("SELECT * FROM recording_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): RecordingSession?

    @Query("SELECT * FROM recording_sessions WHERE endTime IS NULL LIMIT 1")
    suspend fun getActiveSession(): RecordingSession?

    @Query("DELETE FROM recording_sessions")
    suspend fun deleteAll()
}
