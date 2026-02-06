package com.nightsound.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val snippetCount by viewModel.snippetCount.collectAsStateWithLifecycle()
    val chunkDurationSeconds by viewModel.chunkDurationSeconds.collectAsStateWithLifecycle()

    // Periodic save settings
    val periodicSaveEnabled by viewModel.periodicSaveEnabled.collectAsStateWithLifecycle()
    val periodicSaveCount by viewModel.periodicSaveCount.collectAsStateWithLifecycle()
    val periodicSaveIntervalMinutes by viewModel.periodicSaveIntervalMinutes.collectAsStateWithLifecycle()

    // Auto-stop settings
    val autoStopEnabled by viewModel.autoStopEnabled.collectAsStateWithLifecycle()
    val autoStopHour by viewModel.autoStopHour.collectAsStateWithLifecycle()
    val autoStopMinute by viewModel.autoStopMinute.collectAsStateWithLifecycle()

    // Delayed start settings
    val delayedStartEnabled by viewModel.delayedStartEnabled.collectAsStateWithLifecycle()
    val delayedStartMinutes by viewModel.delayedStartMinutes.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Top Snippets: $snippetCount",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Slider(
                        value = snippetCount.toFloat(),
                        onValueChange = { viewModel.setSnippetCount(it.toInt()) },
                        valueRange = 1f..20f,
                        steps = 19
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("20", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Divider()

                    Text(
                        text = "Snippet Length: ${chunkDurationSeconds}s",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Slider(
                        value = chunkDurationSeconds.toFloat(),
                        onValueChange = { viewModel.setChunkDurationSeconds(it.toInt()) },
                        valueRange = 5f..60f,
                        steps = 55
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("5s", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("60s", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Periodic Save card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Periodic Save",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Switch(
                            checked = periodicSaveEnabled,
                            onCheckedChange = { viewModel.setPeriodicSaveEnabled(it) }
                        )
                    }
                    Text(
                        text = "Save the loudest snippets periodically and clear the list to continue collecting",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (periodicSaveEnabled) {
                        Divider()

                        Text(
                            text = "Save top $periodicSaveCount snippets",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Slider(
                            value = periodicSaveCount.toFloat(),
                            onValueChange = { viewModel.setPeriodicSaveCount(it.toInt()) },
                            valueRange = 1f..10f,
                            steps = 9
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("1", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("10", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Divider()

                        Text(
                            text = "Every $periodicSaveIntervalMinutes minutes",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Slider(
                            value = periodicSaveIntervalMinutes.toFloat(),
                            onValueChange = { viewModel.setPeriodicSaveIntervalMinutes(it.toInt()) },
                            valueRange = 15f..180f,
                            steps = 10
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("15m", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("180m", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Auto-stop card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Auto-Stop Recording",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Switch(
                            checked = autoStopEnabled,
                            onCheckedChange = { viewModel.setAutoStopEnabled(it) }
                        )
                    }
                    Text(
                        text = "Automatically stop recording at a set time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (autoStopEnabled) {
                        Divider()

                        Text(
                            text = "Stop at ${String.format("%d:%02d %s", if (autoStopHour % 12 == 0) 12 else autoStopHour % 12, autoStopMinute, if (autoStopHour < 12) "AM" else "PM")}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "Hour",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = autoStopHour.toFloat(),
                            onValueChange = { viewModel.setAutoStopHour(it.toInt()) },
                            valueRange = 0f..23f,
                            steps = 23
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("12 AM", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("11 PM", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Text(
                            text = "Minute",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = autoStopMinute.toFloat(),
                            onValueChange = { viewModel.setAutoStopMinute(it.toInt()) },
                            valueRange = 0f..55f,
                            steps = 10
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(":00", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(":55", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Delayed start card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Delayed Start",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Switch(
                            checked = delayedStartEnabled,
                            onCheckedChange = { viewModel.setDelayedStartEnabled(it) }
                        )
                    }
                    Text(
                        text = "Wait before starting to record after pressing Start",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (delayedStartEnabled) {
                        Divider()

                        Text(
                            text = "Start after $delayedStartMinutes minutes",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Slider(
                            value = delayedStartMinutes.toFloat(),
                            onValueChange = { viewModel.setDelayedStartMinutes(it.toInt()) },
                            valueRange = 5f..120f,
                            steps = 22
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("5m", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("120m", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
