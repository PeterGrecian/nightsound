package com.nightsound.ui.screens.playback

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightsound.data.local.entities.AudioSnippet
import com.nightsound.data.local.entities.RecordingSession
import java.text.SimpleDateFormat
import java.util.Date

private sealed class ListEntry {
    data class Header(val session: RecordingSession, val snippets: List<AudioSnippet>) : ListEntry()
    data class Snippet(val snippet: AudioSnippet, val indexInSession: Int) : ListEntry()
}

private val timeFormat = SimpleDateFormat("h:mm a")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackScreen(
    onNavigateBack: () -> Unit,
    viewModel: PlaybackViewModel = hiltViewModel()
) {
    val snippets by viewModel.snippets.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPlayingId by viewModel.currentPlayingId.collectAsStateWithLifecycle()
    val sortByLoudness by viewModel.sortByLoudness.collectAsStateWithLifecycle()

    // When sorted by time: group by session with timeline headers.
    // When sorted by loudness: flat list, no headers.
    val entries: List<ListEntry> = if (!sortByLoudness) {
        snippets
            .groupBy { it.sessionId }
            .toSortedMap(compareByDescending { it })
            .flatMap { (sessionId, sessionSnippets) ->
                val session = sessions[sessionId]
                val sorted = sessionSnippets.sortedBy { it.timestamp }
                if (session != null) {
                    listOf(ListEntry.Header(session, sorted)) +
                        sorted.mapIndexed { idx, s -> ListEntry.Snippet(s, idx) }
                } else {
                    sorted.mapIndexed { idx, s -> ListEntry.Snippet(s, idx) }
                }
            }
    } else {
        snippets.mapIndexed { idx, s -> ListEntry.Snippet(s, idx) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recordings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (snippets.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearAllRecordings() }) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Clear All")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (snippets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recordings yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !sortByLoudness,
                            onClick = { viewModel.setSortByLoudness(false) },
                            label = { Text("By Time") }
                        )
                        FilterChip(
                            selected = sortByLoudness,
                            onClick = { viewModel.setSortByLoudness(true) },
                            label = { Text("By Loudness") }
                        )
                    }
                }

                items(entries, key = { entry ->
                    when (entry) {
                        is ListEntry.Header -> "header_${entry.session.id}"
                        is ListEntry.Snippet -> "snippet_${entry.snippet.id}"
                    }
                }) { entry ->
                    when (entry) {
                        is ListEntry.Header -> SessionTimeline(
                            session = entry.session,
                            snippets = entry.snippets,
                            currentPlayingId = currentPlayingId
                        )
                        is ListEntry.Snippet -> SnippetCard(
                            snippet = entry.snippet,
                            index = entry.indexInSession,
                            isPlaying = currentPlayingId == entry.snippet.id,
                            onPlayClick = {
                                if (currentPlayingId == entry.snippet.id && isPlaying) {
                                    viewModel.stopPlayback()
                                } else {
                                    viewModel.playSnippet(entry.snippet)
                                }
                            },
                            onDeleteClick = {
                                viewModel.deleteSnippet(entry.snippet)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionTimeline(
    session: RecordingSession,
    snippets: List<AudioSnippet>,
    currentPlayingId: Long?
) {
    val startTime = session.startTime
    val endTime = session.endTime ?: System.currentTimeMillis()
    val duration = endTime - startTime

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    val playingColor = MaterialTheme.colorScheme.error

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Time labels: start — duration — end
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = timeFormat.format(Date(startTime)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDuration(duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = timeFormat.format(Date(endTime)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Timeline track with snippet markers
            Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
                val trackY = size.height / 2f
                val markerRadius = 8.dp.toPx()
                val trackStartX = markerRadius
                val trackEndX = size.width - markerRadius
                val trackWidth = trackEndX - trackStartX

                // Track line
                drawLine(
                    color = trackColor,
                    start = Offset(trackStartX, trackY),
                    end = Offset(trackEndX, trackY),
                    strokeWidth = 3.dp.toPx()
                )

                // Snippet markers
                for (snippet in snippets) {
                    val fraction = if (duration > 0) {
                        ((snippet.timestamp - startTime).toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                    } else 0.5f

                    val x = trackStartX + fraction * trackWidth
                    val color = if (snippet.id == currentPlayingId) playingColor else primaryColor

                    drawCircle(
                        color = color,
                        radius = markerRadius,
                        center = Offset(x, trackY)
                    )
                }
            }
        }
    }
}

@Composable
fun SnippetCard(
    snippet: AudioSnippet,
    index: Int,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPlayClick) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Snippet ${index + 1}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = formatRelativeTime(snippet.timestamp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Loudness: ${String.format("%.0f", snippet.rmsValue * 10000 - 50)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / 60_000
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "$minutes minute${if (minutes != 1L) "s" else ""} ago"
        hours < 24 -> "$hours hour${if (hours != 1L) "s" else ""} ago"
        else -> "$days day${if (days != 1L) "s" else ""} ago"
    }
}

private fun formatDuration(millis: Long): String {
    val minutes = millis / 60_000
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
}
