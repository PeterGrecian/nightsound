package com.nightsound.ui.screens.playback

import android.app.Application
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nightsound.data.local.entities.AudioSnippet
import com.nightsound.data.local.entities.RecordingSession
import com.nightsound.data.repository.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    application: Application,
    private val audioRepository: AudioRepository
) : AndroidViewModel(application) {

    private val TAG = "PlaybackViewModel"
    private val context = application.applicationContext

    private val _sortByLoudness = MutableStateFlow(false)
    val sortByLoudness: StateFlow<Boolean> = _sortByLoudness

    val snippets: StateFlow<List<AudioSnippet>> = combine(
        audioRepository.getAllSnippets(),
        _sortByLoudness
    ) { snippets, byLoudness ->
        if (byLoudness) snippets.sortedByDescending { it.rmsValue }
        else snippets // already timestamp DESC from Room
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val sessions: StateFlow<Map<Long, RecordingSession>> = audioRepository.getAllSessions()
        .map { list -> list.associateBy { it.id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPlayingId = MutableStateFlow<Long?>(null)
    val currentPlayingId: StateFlow<Long?> = _currentPlayingId

    private var mediaPlayer: MediaPlayer? = null

    fun playSnippet(snippet: AudioSnippet) {
        try {
            // Stop current playback if any
            stopPlayback()

            val cacheDir = File(context.cacheDir, "audio_recordings")
            val file = File(cacheDir, snippet.fileName)

            if (!file.exists()) {
                Log.e(TAG, "Audio file not found: ${file.path}")
                return
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.path)
                prepare()
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPlayingId.value = null
                }
                start()
            }

            _isPlaying.value = true
            _currentPlayingId.value = snippet.id
            Log.d(TAG, "Playing snippet: ${snippet.fileName}")

        } catch (e: Exception) {
            Log.e(TAG, "Error playing snippet", e)
            _isPlaying.value = false
            _currentPlayingId.value = null
        }
    }

    fun stopPlayback() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        _isPlaying.value = false
        _currentPlayingId.value = null
        Log.d(TAG, "Stopped playback")
    }

    fun deleteSnippet(snippet: AudioSnippet) {
        viewModelScope.launch {
            try {
                val cacheDir = File(context.cacheDir, "audio_recordings")
                val file = File(cacheDir, snippet.fileName)
                if (file.exists()) {
                    file.delete()
                }
                audioRepository.deleteSnippet(snippet)
                Log.d(TAG, "Deleted snippet: ${snippet.fileName}")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting snippet", e)
            }
        }
    }

    fun clearAllRecordings() {
        stopPlayback()
        viewModelScope.launch {
            try {
                val cacheDir = File(context.cacheDir, "audio_recordings")
                cacheDir.listFiles()?.forEach { it.delete() }
                audioRepository.clearAll()
                Log.d(TAG, "Cleared all recordings")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing recordings", e)
            }
        }
    }

    fun setSortByLoudness(byLoudness: Boolean) {
        _sortByLoudness.value = byLoudness
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
    }
}
