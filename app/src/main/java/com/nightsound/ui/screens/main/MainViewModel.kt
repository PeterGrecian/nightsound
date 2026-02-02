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
import com.nightsound.service.AudioRecordingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application
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
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Service disconnected")
            recordingService = null
            serviceBound = false
        }
    }

    init {
        bindService()
    }

    private fun bindService() {
        val intent = Intent(context, AudioRecordingService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun startRecording() {
        Log.d(TAG, "Starting recording")
        val intent = Intent(context, AudioRecordingService::class.java).apply {
            action = AudioRecordingService.ACTION_START_RECORDING
        }
        context.startForegroundService(intent)
    }

    fun stopRecording() {
        Log.d(TAG, "Stopping recording")
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
