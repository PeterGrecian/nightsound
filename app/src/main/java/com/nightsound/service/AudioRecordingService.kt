package com.nightsound.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.nightsound.MainActivity
import com.nightsound.R
import com.nightsound.data.local.database.NightSoundDatabase
import com.nightsound.data.local.entities.AudioSnippet
import com.nightsound.data.local.entities.RecordingSession
import com.nightsound.service.audio.AudioFileWriter
import com.nightsound.service.audio.LoudnessAnalyzer
import com.nightsound.service.audio.TopSnippetsManager
import com.nightsound.data.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class AudioRecordingService : Service() {

    @Inject
    lateinit var database: NightSoundDatabase

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val TAG = "AudioRecordingService"
    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "audio_recording_channel"

    // Audio configuration
    private val SAMPLE_RATE = 16000
    private val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var wakeLock: PowerManager.WakeLock
    private var currentSessionId: Long = -1

    // Audio processing components
    private val loudnessAnalyzer = LoudnessAnalyzer()
    private var topSnippetsManager = TopSnippetsManager(maxSnippets = 3)
    private var recordingDurationSeconds = 10
    private lateinit var audioFileWriter: AudioFileWriter
    private lateinit var audioCacheDir: File

    // State flows for UI updates
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _currentVolume = MutableStateFlow(0.0)
    val currentVolume: StateFlow<Double> = _currentVolume

    private val _snippetCount = MutableStateFlow(0)
    val snippetCount: StateFlow<Int> = _snippetCount

    private val _currentSnippets = MutableStateFlow<List<Pair<Long, Double>>>(emptyList())
    val currentSnippets: StateFlow<List<Pair<Long, Double>>> = _currentSnippets

    private val _recordingStartTime = MutableStateFlow<Long?>(null)
    val recordingStartTime: StateFlow<Long?> = _recordingStartTime

    inner class LocalBinder : Binder() {
        fun getService(): AudioRecordingService = this@AudioRecordingService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        // Initialize audio file writer
        audioFileWriter = AudioFileWriter(SAMPLE_RATE, 1, 16)

        // Setup cache directory
        audioCacheDir = File(getCacheDir(), "audio_recordings")
        if (!audioCacheDir.exists()) {
            audioCacheDir.mkdirs()
        }

        // Acquire wake lock
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "NightSound::RecordingWakeLock"
        )

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RECORDING -> startRecording()
            ACTION_STOP_RECORDING -> stopRecording()
        }
        return START_STICKY
    }

    private fun startRecording() {
        if (_isRecording.value) {
            Log.w(TAG, "Already recording")
            return
        }

        Log.d(TAG, "Starting recording")

        // Start foreground service
        startForeground(NOTIFICATION_ID, createNotification())

        // Acquire wake lock
        if (!wakeLock.isHeld) {
            wakeLock.acquire(12 * 60 * 60 * 1000L) // 12 hours max
        }

        _isRecording.value = true

        recordingJob = scope.launch {
            // Read settings
            recordingDurationSeconds = settingsRepository.chunkDurationSeconds.first()
            topSnippetsManager = TopSnippetsManager(maxSnippets = settingsRepository.snippetCount.first())

            // Create recording session in database
            val session = RecordingSession(
                startTime = System.currentTimeMillis(),
                endTime = null,
                snippetCount = 0
            )
            currentSessionId = database.recordingSessionDao().insert(session)
            _recordingStartTime.value = session.startTime
            Log.d(TAG, "Created recording session: $currentSessionId")

            // Initialize AudioRecord
            val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            recordAudio()
        }
    }

    private suspend fun recordAudio() {
        val audioRecord = audioRecord ?: return

        try {
            audioRecord.startRecording()
            Log.d(TAG, "AudioRecord started")

            val samplesPerChunk = SAMPLE_RATE * recordingDurationSeconds
            val buffer = ShortArray(samplesPerChunk)
            var chunkNumber = 0

            while (_isRecording.value && coroutineContext.isActive) {
                // Read 10 seconds of audio
                var totalSamplesRead = 0
                while (totalSamplesRead < samplesPerChunk && _isRecording.value) {
                    val samplesRead = audioRecord.read(
                        buffer,
                        totalSamplesRead,
                        samplesPerChunk - totalSamplesRead
                    )

                    if (samplesRead > 0) {
                        // Update volume live on each read for UI feedback
                        val rms = loudnessAnalyzer.calculateRMS(
                            buffer.copyOfRange(totalSamplesRead, totalSamplesRead + samplesRead)
                        )
                        _currentVolume.value = rms
                        totalSamplesRead += samplesRead
                    } else {
                        Log.e(TAG, "Error reading audio: $samplesRead")
                        break
                    }
                }

                if (totalSamplesRead == samplesPerChunk) {
                    // Calculate RMS loudness for the full chunk (used for snippet ranking)
                    val rms = loudnessAnalyzer.calculateRMS(buffer)

                    // Save to file
                    val timestamp = System.currentTimeMillis()
                    val fileName = "audio_${currentSessionId}_${chunkNumber}_${timestamp}.wav"
                    val file = File(audioCacheDir, fileName)

                    try {
                        audioFileWriter.writeWavFile(file, buffer)

                        // Offer to top snippets manager
                        val accepted = topSnippetsManager.offer(file, rms, timestamp)

                        if (accepted) {
                            _snippetCount.value = topSnippetsManager.getCount()
                            _currentSnippets.value = topSnippetsManager.getTopSnippets()
                                .map { Pair(it.timestamp, it.rmsValue) }
                        }

                        Log.d(TAG, "Chunk $chunkNumber: RMS=$rms, accepted=$accepted")
                        chunkNumber++

                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving audio chunk", e)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error during recording", e)
        } finally {
            audioRecord.stop()
            audioRecord.release()
            Log.d(TAG, "AudioRecord stopped and released")
        }
    }

    private fun stopRecording() {
        if (!_isRecording.value) {
            Log.w(TAG, "Not recording")
            return
        }

        Log.d(TAG, "Stopping recording")
        _isRecording.value = false
        _currentSnippets.value = emptyList()
        _recordingStartTime.value = null

        // Cancel recording job
        recordingJob?.cancel()
        recordingJob = null

        // Release wake lock
        if (wakeLock.isHeld) {
            wakeLock.release()
        }

        // Finalize top snippets and save to database
        scope.launch {
            val topSnippets = topSnippetsManager.finalizeTopSnippets()
            Log.d(TAG, "Finalized ${topSnippets.size} top snippets")

            val snippetEntities = topSnippets.map { snippetData ->
                AudioSnippet(
                    fileName = snippetData.file.name,
                    timestamp = snippetData.timestamp,
                    rmsValue = snippetData.rmsValue,
                    sessionId = currentSessionId
                )
            }

            database.audioSnippetDao().insertAll(snippetEntities)

            // Update recording session
            val session = database.recordingSessionDao().getSessionById(currentSessionId)
            if (session != null) {
                database.recordingSessionDao().update(
                    session.copy(
                        endTime = System.currentTimeMillis(),
                        snippetCount = topSnippets.size
                    )
                )
            }

            Log.d(TAG, "Saved ${snippetEntities.size} snippets to database")

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Audio Recording",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Ongoing audio recording notification"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NightSound Recording")
            .setContentText("Recording audio in the background")
            .setSmallIcon(R.drawable.ic_mic)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (_isRecording.value) {
            stopRecording()
        }
        scope.cancel()
        Log.d(TAG, "Service destroyed")
    }

    companion object {
        const val ACTION_START_RECORDING = "com.nightsound.START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.nightsound.STOP_RECORDING"
    }
}
