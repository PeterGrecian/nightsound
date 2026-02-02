package com.nightsound.ui.screens.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightsound.ui.theme.VolumeGreen
import com.nightsound.ui.theme.VolumeRed
import com.nightsound.ui.theme.VolumeYellow
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToPlayback: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val currentVolume by viewModel.currentVolume.collectAsStateWithLifecycle()
    val snippetCount by viewModel.snippetCount.collectAsStateWithLifecycle()

    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val recordPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasRecordPermission = isGranted
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NightSound") },
                actions = {
                    IconButton(onClick = onNavigateToPlayback) {
                        Icon(Icons.Default.List, contentDescription = "Recordings")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Status
            Text(
                text = if (isRecording) "Recording..." else "Ready",
                style = MaterialTheme.typography.headlineMedium,
                color = if (isRecording) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Snippet count
            if (isRecording) {
                Text(
                    text = "Top snippets: $snippetCount / 10",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Volume visualizer
            VolumeVisualizer(
                volume = currentVolume,
                isActive = isRecording,
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Start/Stop button
            Button(
                onClick = {
                    if (!hasRecordPermission) {
                        recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    } else if (!hasNotificationPermission) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        if (isRecording) {
                            viewModel.stopRecording()
                        } else {
                            viewModel.startRecording()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isRecording) "Stop Recording" else "Start Recording",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Permission warning
            if (!hasRecordPermission || !hasNotificationPermission) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Microphone and notification permissions required",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun VolumeVisualizer(
    volume: Double,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = min(size.width, size.height) / 2

        // Background circle
        drawCircle(
            color = Color.Gray.copy(alpha = 0.2f),
            radius = maxRadius,
            center = Offset(centerX, centerY)
        )

        if (isActive) {
            // Volume circle
            val volumeRadius = (volume * maxRadius).toFloat().coerceIn(0f, maxRadius)
            val volumeColor = when {
                volume < 0.3 -> VolumeGreen
                volume < 0.6 -> VolumeYellow
                else -> VolumeRed
            }

            drawCircle(
                color = volumeColor.copy(alpha = 0.7f),
                radius = volumeRadius,
                center = Offset(centerX, centerY)
            )
        }
    }
}
