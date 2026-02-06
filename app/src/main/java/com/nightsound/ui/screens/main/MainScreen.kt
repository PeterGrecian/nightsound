package com.nightsound.ui.screens.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightsound.ui.theme.VolumeGreen
import com.nightsound.ui.theme.VolumeRed
import com.nightsound.ui.theme.VolumeYellow
import java.text.SimpleDateFormat
import java.util.Date
import kotlinx.coroutines.delay

private val absTimeFormat = SimpleDateFormat("h:mm:ss a")

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
    val maxSnippets by viewModel.maxSnippets.collectAsStateWithLifecycle()
    val currentSnippets by viewModel.currentSnippets.collectAsStateWithLifecycle()
    val recordingStartTime by viewModel.recordingStartTime.collectAsStateWithLifecycle()
    val delayedStartCountdown by viewModel.delayedStartCountdownSeconds.collectAsStateWithLifecycle()

    var showInfoDialog by remember { mutableStateOf(false) }

    // Tick every second so elapsed times in the snippet list stay fresh
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = System.currentTimeMillis()
        }
    }

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
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "How it works")
                    }
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status — red when recording, tertiary during countdown, default when idle
            Text(
                text = when {
                    delayedStartCountdown != null -> "Starting in ${formatElapsed(delayedStartCountdown!! * 1000L)}"
                    isRecording -> "Recording..."
                    else -> "Ready"
                },
                style = MaterialTheme.typography.headlineMedium,
                color = when {
                    delayedStartCountdown != null -> MaterialTheme.colorScheme.tertiary
                    isRecording -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            // Snippet count — only while filling up (hide once heap is full)
            if (isRecording && snippetCount < maxSnippets) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Top snippets: $snippetCount / $maxSnippets",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Volume bar chart
            VolumeBarChart(
                volume = currentVolume,
                isActive = isRecording,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Live snippet list — only during recording
            if (isRecording && currentSnippets.isNotEmpty()) {
                SnippetList(
                    snippets = currentSnippets,
                    recordingStartTime = recordingStartTime,
                    now = now,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Push button to bottom
            Spacer(modifier = Modifier.weight(1f))

            Spacer(modifier = Modifier.height(16.dp))

            // Start/Stop button
            Button(
                onClick = {
                    if (delayedStartCountdown != null) {
                        viewModel.cancelDelayedStart()
                    } else if (!hasRecordPermission) {
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
                    containerColor = when {
                        delayedStartCountdown != null -> MaterialTheme.colorScheme.tertiary
                        isRecording -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                Text(
                    text = when {
                        delayedStartCountdown != null -> "Cancel"
                        isRecording -> "Stop Recording"
                        else -> "Start Recording"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Permission warning
            if (!hasRecordPermission || !hasNotificationPermission) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Microphone and notification permissions required",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("How Snippet Selection Works") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Audio is recorded continuously in fixed-length chunks. " +
                        "The chunk length is set in Settings (default 10 seconds)."
                    )
                    Text(
                        "Each chunk's loudness is measured using RMS (root mean square) — " +
                        "a standard way to calculate the average energy of a sound over time."
                    )
                    Text(
                        "NightSound keeps only the N loudest chunks using a rolling tournament. " +
                        "As each new chunk finishes recording, it competes against the quietest " +
                        "chunk currently saved. If the new chunk is louder, it replaces it. " +
                        "Otherwise it is discarded immediately."
                    )
                    Text(
                        "This means at any point during recording, only the top N loudest " +
                        "chunks are kept in memory. When you stop recording, those N chunks " +
                        "are saved as your snippets."
                    )
                    Text(
                        "Both the number of snippets (N) and the chunk length are configurable " +
                        "in Settings."
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
private fun SnippetList(
    snippets: List<Pair<Long, Double>>,  // (timestamp, rmsValue)
    recordingStartTime: Long?,
    now: Long,
    modifier: Modifier = Modifier
) {
    // Most recent snippet first
    val sorted = snippets.sortedByDescending { it.first }
    val useGrid = sorted.size > 8

    Column(modifier = modifier) {
        if (useGrid) {
            // Two columns for large N
            val half = (sorted.size + 1) / 2
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    for (snippet in sorted.take(half)) {
                        SnippetRow(snippet, now, small = true)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    for (snippet in sorted.drop(half)) {
                        SnippetRow(snippet, now, small = true)
                    }
                }
            }
        } else {
            for (snippet in sorted) {
                SnippetRow(snippet, now, small = false)
            }
        }

        // Recording start time footer
        if (recordingStartTime != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Started at ${absTimeFormat.format(Date(recordingStartTime))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SnippetRow(snippet: Pair<Long, Double>, now: Long, small: Boolean) {
    val elapsed = now - snippet.first
    val style = if (small) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = formatElapsed(elapsed),
            style = style,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = String.format("%.0f", snippet.second * 10000 - 50),
            style = style
        )
        Text(
            text = absTimeFormat.format(Date(snippet.first)),
            style = style,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatElapsed(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}

@Composable
fun VolumeBarChart(
    volume: Double,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val barCount = 100
    // Scale factor: maps typical RMS range (0.0–0.125) onto 0.0–1.0 display range
    val scale = 8.0

    val history = remember { mutableStateListOf<Double>() }

    LaunchedEffect(isActive) {
        if (!isActive) history.clear()
    }

    LaunchedEffect(volume) {
        if (isActive) {
            if (history.size >= barCount) history.removeAt(0)
            history.add(volume)
        }
    }

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val barSpacing = size.width / barCount
            val barWidth = barSpacing * 0.7f
            val gap = barSpacing - barWidth
            val maxBarHeight = size.height

            val offset = barCount - history.size
            for (i in 0 until barCount) {
                val histIdx = i - offset
                val rawValue = if (histIdx in history.indices) history[histIdx] else 0.0
                val displayValue = (rawValue * scale).coerceIn(0.0, 1.0)
                val barHeight = (displayValue * maxBarHeight).toFloat()
                val x = i * barSpacing + gap / 2f

                if (barHeight > 1f) {
                    val color = when {
                        displayValue < 0.3 -> VolumeGreen
                        displayValue < 0.7 -> VolumeYellow
                        else -> VolumeRed
                    }
                    drawRect(
                        color = color.copy(alpha = 0.85f),
                        topLeft = Offset(x, size.height - barHeight),
                        size = Size(barWidth, barHeight)
                    )
                } else {
                    // Flat baseline stub
                    drawRect(
                        color = Color.Gray.copy(alpha = 0.25f),
                        topLeft = Offset(x, size.height - 2f),
                        size = Size(barWidth, 2f)
                    )
                }
            }
        }

        // Time axis labels
        if (isActive) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "← older",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "now",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}
