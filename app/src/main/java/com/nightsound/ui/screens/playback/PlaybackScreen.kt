package com.nightsound.ui.screens.playback

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nightsound.data.local.entities.AudioSnippet
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackScreen(
    onNavigateBack: () -> Unit,
    viewModel: PlaybackViewModel = hiltViewModel()
) {
    val snippets by viewModel.snippets.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPlayingId by viewModel.currentPlayingId.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recordings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                items(snippets, key = { it.id }) { snippet ->
                    SnippetCard(
                        snippet = snippet,
                        isPlaying = currentPlayingId == snippet.id,
                        onPlayClick = {
                            if (currentPlayingId == snippet.id && isPlaying) {
                                viewModel.stopPlayback()
                            } else {
                                viewModel.playSnippet(snippet)
                            }
                        },
                        onDeleteClick = {
                            viewModel.deleteSnippet(snippet)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SnippetCard(
    snippet: AudioSnippet,
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
            // Play/Pause button
            IconButton(onClick = onPlayClick) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Snippet info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = formatTimestamp(snippet.timestamp),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Loudness: ${String.format("%.4f", snippet.rmsValue)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (snippet.uploadedToS3) "Uploaded" else "Not uploaded",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (snippet.uploadedToS3)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }

            // Delete button
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

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
    return format.format(date)
}
