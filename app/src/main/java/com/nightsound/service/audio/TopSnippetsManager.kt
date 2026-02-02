package com.nightsound.service.audio

import android.util.Log
import java.io.File
import java.util.PriorityQueue

/**
 * Manages the top 10 loudest audio snippets using a min heap (priority queue).
 * Automatically deletes files that don't make the top 10 to save storage.
 */
class TopSnippetsManager(private val maxSnippets: Int = 10) {

    private val TAG = "TopSnippetsManager"

    // Min heap - smallest RMS value at the top
    private val topSnippets = PriorityQueue<AudioSnippetData>(maxSnippets)

    /**
     * Offer a new snippet. If it's in the top 10, keep it. Otherwise, delete it.
     *
     * @param file The audio file
     * @param rmsValue The RMS loudness value
     * @param timestamp The recording timestamp
     * @return true if the snippet was added to top 10, false if rejected
     */
    @Synchronized
    fun offer(file: File, rmsValue: Double, timestamp: Long): Boolean {
        val snippet = AudioSnippetData(file, rmsValue, timestamp)

        return if (topSnippets.size < maxSnippets) {
            // Not full yet, add it
            topSnippets.offer(snippet)
            Log.d(TAG, "Added snippet: ${file.name}, RMS: $rmsValue (${topSnippets.size}/$maxSnippets)")
            true
        } else {
            // Check if this snippet is louder than the quietest in the top 10
            val quietest = topSnippets.peek()
            if (quietest != null && rmsValue > quietest.rmsValue) {
                // Remove the quietest and delete its file
                val removed = topSnippets.poll()
                if (removed != null) {
                    deleteFile(removed.file)
                    Log.d(TAG, "Removed quieter snippet: ${removed.file.name}, RMS: ${removed.rmsValue}")
                }

                // Add the new louder snippet
                topSnippets.offer(snippet)
                Log.d(TAG, "Added louder snippet: ${file.name}, RMS: $rmsValue")
                true
            } else {
                // This snippet is not loud enough, delete it immediately
                deleteFile(file)
                Log.d(TAG, "Rejected snippet: ${file.name}, RMS: $rmsValue (not in top 10)")
                false
            }
        }
    }

    /**
     * Get all top snippets, sorted by RMS (loudest first).
     */
    @Synchronized
    fun getTopSnippets(): List<AudioSnippetData> {
        return topSnippets.sortedByDescending { it.rmsValue }
    }

    /**
     * Get the current count of top snippets.
     */
    @Synchronized
    fun getCount(): Int = topSnippets.size

    /**
     * Clear all snippets and delete remaining files if cleanup is requested.
     */
    @Synchronized
    fun clear(deleteFiles: Boolean = false) {
        if (deleteFiles) {
            topSnippets.forEach { deleteFile(it.file) }
        }
        topSnippets.clear()
    }

    /**
     * Finalize the recording session and delete any files not in the top snippets.
     * This should be called after recording stops.
     */
    @Synchronized
    fun finalizeTopSnippets(): List<AudioSnippetData> {
        val finalList = getTopSnippets()
        Log.d(TAG, "Finalized ${finalList.size} top snippets")
        return finalList
    }

    private fun deleteFile(file: File) {
        try {
            if (file.exists() && file.delete()) {
                Log.d(TAG, "Deleted file: ${file.name}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file: ${file.name}", e)
        }
    }
}
