package com.nightsound.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recording_sessions")
data class RecordingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val snippetCount: Int = 0
)
