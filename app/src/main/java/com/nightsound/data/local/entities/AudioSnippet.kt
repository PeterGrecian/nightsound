package com.nightsound.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_snippets")
data class AudioSnippet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val timestamp: Long,
    val rmsValue: Double,
    val uploadedToS3: Boolean = false,
    val s3Key: String? = null,
    val sessionId: Long
)
