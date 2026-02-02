package com.nightsound.data.local.dao

import androidx.room.*
import com.nightsound.data.local.entities.AudioSnippet
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioSnippetDao {

    @Insert
    suspend fun insert(snippet: AudioSnippet): Long

    @Insert
    suspend fun insertAll(snippets: List<AudioSnippet>)

    @Update
    suspend fun update(snippet: AudioSnippet)

    @Delete
    suspend fun delete(snippet: AudioSnippet)

    @Query("SELECT * FROM audio_snippets WHERE sessionId = :sessionId ORDER BY rmsValue DESC")
    fun getSnippetsBySession(sessionId: Long): Flow<List<AudioSnippet>>

    @Query("SELECT * FROM audio_snippets ORDER BY timestamp DESC")
    fun getAllSnippets(): Flow<List<AudioSnippet>>

    @Query("SELECT * FROM audio_snippets WHERE uploadedToS3 = 0")
    suspend fun getUnuploadedSnippets(): List<AudioSnippet>

    @Query("UPDATE audio_snippets SET uploadedToS3 = 1, s3Key = :s3Key WHERE id = :snippetId")
    suspend fun markAsUploaded(snippetId: Long, s3Key: String)

    @Query("SELECT * FROM audio_snippets WHERE id = :id")
    suspend fun getSnippetById(id: Long): AudioSnippet?
}
