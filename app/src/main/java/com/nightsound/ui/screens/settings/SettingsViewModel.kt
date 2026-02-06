package com.nightsound.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightsound.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _snippetCount = MutableStateFlow(3)
    val snippetCount: StateFlow<Int> = _snippetCount

    private val _chunkDurationSeconds = MutableStateFlow(10)
    val chunkDurationSeconds: StateFlow<Int> = _chunkDurationSeconds

    // Periodic save settings
    private val _periodicSaveEnabled = MutableStateFlow(false)
    val periodicSaveEnabled: StateFlow<Boolean> = _periodicSaveEnabled

    private val _periodicSaveCount = MutableStateFlow(2)
    val periodicSaveCount: StateFlow<Int> = _periodicSaveCount

    private val _periodicSaveIntervalMinutes = MutableStateFlow(60)
    val periodicSaveIntervalMinutes: StateFlow<Int> = _periodicSaveIntervalMinutes

    // Auto-stop settings
    private val _autoStopEnabled = MutableStateFlow(false)
    val autoStopEnabled: StateFlow<Boolean> = _autoStopEnabled

    private val _autoStopHour = MutableStateFlow(6)
    val autoStopHour: StateFlow<Int> = _autoStopHour

    private val _autoStopMinute = MutableStateFlow(0)
    val autoStopMinute: StateFlow<Int> = _autoStopMinute

    // Delayed start settings
    private val _delayedStartEnabled = MutableStateFlow(false)
    val delayedStartEnabled: StateFlow<Boolean> = _delayedStartEnabled

    private val _delayedStartMinutes = MutableStateFlow(30)
    val delayedStartMinutes: StateFlow<Int> = _delayedStartMinutes

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.snippetCount.collectLatest { _snippetCount.value = it }
        }
        viewModelScope.launch {
            settingsRepository.chunkDurationSeconds.collectLatest { _chunkDurationSeconds.value = it }
        }
        viewModelScope.launch {
            settingsRepository.periodicSaveEnabled.collectLatest { _periodicSaveEnabled.value = it }
        }
        viewModelScope.launch {
            settingsRepository.periodicSaveCount.collectLatest { _periodicSaveCount.value = it }
        }
        viewModelScope.launch {
            settingsRepository.periodicSaveIntervalMinutes.collectLatest { _periodicSaveIntervalMinutes.value = it }
        }
        viewModelScope.launch {
            settingsRepository.autoStopEnabled.collectLatest { _autoStopEnabled.value = it }
        }
        viewModelScope.launch {
            settingsRepository.autoStopHour.collectLatest { _autoStopHour.value = it }
        }
        viewModelScope.launch {
            settingsRepository.autoStopMinute.collectLatest { _autoStopMinute.value = it }
        }
        viewModelScope.launch {
            settingsRepository.delayedStartEnabled.collectLatest { _delayedStartEnabled.value = it }
        }
        viewModelScope.launch {
            settingsRepository.delayedStartMinutes.collectLatest { _delayedStartMinutes.value = it }
        }
    }

    fun setSnippetCount(count: Int) {
        viewModelScope.launch {
            settingsRepository.setSnippetCount(count)
        }
    }

    fun setChunkDurationSeconds(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.setChunkDurationSeconds(seconds)
        }
    }

    fun setPeriodicSaveEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setPeriodicSaveEnabled(enabled)
        }
    }

    fun setPeriodicSaveCount(count: Int) {
        viewModelScope.launch {
            settingsRepository.setPeriodicSaveCount(count)
        }
    }

    fun setPeriodicSaveIntervalMinutes(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setPeriodicSaveIntervalMinutes(minutes)
        }
    }

    fun setAutoStopEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoStopEnabled(enabled)
        }
    }

    fun setAutoStopHour(hour: Int) {
        viewModelScope.launch {
            settingsRepository.setAutoStopHour(hour)
        }
    }

    fun setAutoStopMinute(minute: Int) {
        viewModelScope.launch {
            settingsRepository.setAutoStopMinute(minute)
        }
    }

    fun setDelayedStartEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDelayedStartEnabled(enabled)
        }
    }

    fun setDelayedStartMinutes(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setDelayedStartMinutes(minutes)
        }
    }
}
