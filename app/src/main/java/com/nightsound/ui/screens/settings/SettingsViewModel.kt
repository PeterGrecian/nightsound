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
}
