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

    private val _nightStartHour = MutableStateFlow(22)
    val nightStartHour: StateFlow<Int> = _nightStartHour

    private val _nightStartMinute = MutableStateFlow(0)
    val nightStartMinute: StateFlow<Int> = _nightStartMinute

    private val _nightEndHour = MutableStateFlow(7)
    val nightEndHour: StateFlow<Int> = _nightEndHour

    private val _nightEndMinute = MutableStateFlow(0)
    val nightEndMinute: StateFlow<Int> = _nightEndMinute

    private val _s3BucketName = MutableStateFlow("")
    val s3BucketName: StateFlow<String> = _s3BucketName

    private val _s3Region = MutableStateFlow("us-east-1")
    val s3Region: StateFlow<String> = _s3Region

    private val _awsAccessKey = MutableStateFlow("")
    val awsAccessKey: StateFlow<String> = _awsAccessKey

    private val _awsSecretKey = MutableStateFlow("")
    val awsSecretKey: StateFlow<String> = _awsSecretKey

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.nightStartHour.collectLatest { _nightStartHour.value = it }
        }
        viewModelScope.launch {
            settingsRepository.nightStartMinute.collectLatest { _nightStartMinute.value = it }
        }
        viewModelScope.launch {
            settingsRepository.nightEndHour.collectLatest { _nightEndHour.value = it }
        }
        viewModelScope.launch {
            settingsRepository.nightEndMinute.collectLatest { _nightEndMinute.value = it }
        }
        viewModelScope.launch {
            settingsRepository.s3BucketName.collectLatest { _s3BucketName.value = it }
        }
        viewModelScope.launch {
            settingsRepository.s3Region.collectLatest { _s3Region.value = it }
        }
        viewModelScope.launch {
            settingsRepository.awsAccessKey.collectLatest { _awsAccessKey.value = it }
        }
        viewModelScope.launch {
            settingsRepository.awsSecretKey.collectLatest { _awsSecretKey.value = it }
        }
    }

    fun setNightStartTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepository.setNightStartTime(hour, minute)
        }
    }

    fun setNightEndTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepository.setNightEndTime(hour, minute)
        }
    }

    fun setS3BucketName(bucketName: String) {
        viewModelScope.launch {
            settingsRepository.setS3BucketName(bucketName)
        }
    }

    fun setS3Region(region: String) {
        viewModelScope.launch {
            settingsRepository.setS3Region(region)
        }
    }

    fun setAwsCredentials(accessKey: String, secretKey: String) {
        viewModelScope.launch {
            settingsRepository.setAwsCredentials(accessKey, secretKey)
        }
    }
}
