package com.nightsound.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nightsound.data.local.dao.AudioSnippetDao
import com.nightsound.data.local.dao.RecordingSessionDao
import com.nightsound.data.local.entities.AudioSnippet
import com.nightsound.data.local.entities.RecordingSession

@Database(
    entities = [AudioSnippet::class, RecordingSession::class],
    version = 2,
    exportSchema = false
)
abstract class NightSoundDatabase : RoomDatabase() {
    abstract fun audioSnippetDao(): AudioSnippetDao
    abstract fun recordingSessionDao(): RecordingSessionDao
}
