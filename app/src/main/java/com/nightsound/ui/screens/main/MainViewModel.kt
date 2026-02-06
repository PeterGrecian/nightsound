package com.nightsound.ui.screens.main

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nightsound.data.repository.SettingsRepository
import com.nightsound.service.AudioRecordingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    private val TAG = "MainViewModel"
    private val context: Context = application.applicationContext

    private var recordingService: AudioRecordingService? = null
    private var serviceBound = false

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _currentVolume = MutableStateFlow(0.0)
    val currentVolume: StateFlow<Double> = _currentVolume

    private val _snippetCount = MutableStateFlow(0)
    val snippetCount: StateFlow<Int> = _snippetCount

    private val _maxSnippets = MutableStateFlow(3)
    val maxSnippets: StateFlow<Int> = _maxSnippets

    private val _currentSnippets = MutableStateFlow<List<Pair<Long, Double>>>(emptyList())
    val currentSnippets: StateFlow<List<Pair<Long, Double>>> = _currentSnippets

    private val _recordingStartTime = MutableStateFlow<Long?>(null)
    val recordingStartTime: StateFlow<Long?> = _recordingStartTime

    // Delayed start state
    private val _delayedStartCountdownSeconds = MutableStateFlow<Int?>(null)
    val delayedStartCountdownSeconds: StateFlow<Int?> = _delayedStartCountdownSeconds
    private var delayedStartJob: Job? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            Log.d(TAG, "Service connected")
            val localBinder = binder as AudioRecordingService.LocalBinder
            recordingService = localBinder.getService()
            serviceBound = true

            // Observe service state
            viewModelScope.launch {
                recordingService?.isRecording?.collect { isRecording ->
                    _isRecording.value = isRecording
                }
            }

            viewModelScope.launch {
                recordingService?.currentVolume?.collect { volume ->
                    _currentVolume.value = volume
                }
            }

            viewModelScope.launch {
                recordingService?.snippetCount?.collect { count ->
                    _snippetCount.value = count
                }
            }

            viewModelScope.launch {
                recordingService?.currentSnippets?.collect {
                    _currentSnippets.value = it
                }
            }

            viewModelScope.launch {
                recordingService?.recordingStartTime?.collect {
                    _recordingStartTime.value = it
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Service disconnected")
            recordingService = null
            serviceBound = false
        }
    }

    init {
        bindService()
        viewModelScope.launch {
            settingsRepository.snippetCount.collectLatest { _maxSnippets.value = it }
        }
    }

    private fun bindService() {
        val intent = Intent(context, AudioRecordingService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun startRecording() {
        viewModelScope.launch {
            val delayedStartEnabled = settingsRepository.delayedStartEnabled.first()
            if (delayedStartEnabled) {
                val delayMinutes = settingsRepository.delayedStartMinutes.first()
                Log.d(TAG, "Delayed start: waiting $delayMinutes minutes")
                startDelayedRecording(delayMinutes)
            } else {
                startRecordingNow()
            }
        }
    }

    private fun startDelayedRecording(delayMinutes: Int) {
        delayedStartJob = viewModelScope.launch {
            var remainingSeconds = delayMinutes * 60
            _delayedStartCountdownSeconds.value = remainingSeconds
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
                _delayedStartCountdownSeconds.value = remainingSeconds
            }
            _delayedStartCountdownSeconds.value = null
            startRecordingNow()
        }
    }

    private fun startRecordingNow() {
        Log.d(TAG, "Starting recording")
        val intent = Intent(context, AudioRecordingService::class.java).apply {
            action = AudioRecordingService.ACTION_START_RECORDING
        }
        context.startForegroundService(intent)
    }

    fun cancelDelayedStart() {
        delayedStartJob?.cancel()
        delayedStartJob = null
        _delayedStartCountdownSeconds.value = null
        Log.d(TAG, "Delayed start cancelled")
    }

    fun stopRecording() {
        Log.d(TAG, "Stopping recording")
        // Cancel any pending delayed start
        cancelDelayedStart()
        val intent = Intent(context, AudioRecordingService::class.java).apply {
            action = AudioRecordingService.ACTION_STOP_RECORDING
        }
        context.startService(intent)
    }

    override fun onCleared() {
        super.onCleared()
        if (serviceBound) {
            context.unbindService(serviceConnection)
            serviceBound = false
        }
    }
}
