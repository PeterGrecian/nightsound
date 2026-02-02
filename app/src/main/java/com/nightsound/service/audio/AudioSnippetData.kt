package com.nightsound.service.audio

import java.io.File

/**
 * Data class representing an audio snippet during recording.
 */
data class AudioSnippetData(
    val file: File,
    val rmsValue: Double,
    val timestamp: Long
) : Comparable<AudioSnippetData> {
    // Compare by RMS value for priority queue (min heap)
    override fun compareTo(other: AudioSnippetData): Int {
        return this.rmsValue.compareTo(other.rmsValue)
    }
}
